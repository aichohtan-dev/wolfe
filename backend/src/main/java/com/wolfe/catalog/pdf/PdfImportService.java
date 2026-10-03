package com.wolfe.catalog.pdf;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.catalog.brand.Brand;
import com.wolfe.catalog.brand.BrandRepository;
import com.wolfe.catalog.adminmodel.ProductCategoryRepository;
import com.wolfe.inventory.InventoryRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfImportService {
    public record PdfImportItemUpdateRequest(String name, String sku, String brand, String category, String subcategory, BigDecimal price, String finish, String material, String color, String size, String dimensions, String description, String imageUrl, String mediaUrls, String extractedVariantsJson, String attributesJson, String notes) {}
    private final PdfImportJobRepository jobRepo;
    private final PdfImportItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final BrandRepository brandRepo;
    private final ProductCategoryRepository categoryRepo;
    private final InventoryRepository inventoryRepo;
    private final TaskExecutor pdfImportExecutor;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int MAX_PAGES = 300;
    private static final int MAX_IMAGES_PER_PAGE = 20;
    private static final long MAX_IMAGE_PIXELS = 20_000_000L;
    private static final String UPLOAD_DIR = "public/catalog/imports"; // private staging area
    private static final String PUBLISHED_DIR = "public/catalog/published";

    public PdfImportService(PdfImportJobRepository jobRepo, PdfImportItemRepository itemRepo,
                            ProductRepository productRepo, ProductVariantRepository variantRepo,
                            BrandRepository brandRepo, ProductCategoryRepository categoryRepo, InventoryRepository inventoryRepo,
                            @org.springframework.beans.factory.annotation.Qualifier("pdfImportExecutor") TaskExecutor pdfImportExecutor) {
        this.jobRepo = jobRepo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
        this.brandRepo = brandRepo;
        this.categoryRepo = categoryRepo;
        this.inventoryRepo = inventoryRepo;
        this.pdfImportExecutor = pdfImportExecutor;
    }

    @Transactional
    public PdfImportJob uploadAndProcess(MultipartFile file, Long defaultBrandId, Long defaultCategoryId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "catalog.pdf";
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files (.pdf) are supported");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equalsIgnoreCase("application/pdf") && !contentType.equalsIgnoreCase("application/octet-stream")) {
            throw new IllegalArgumentException("Invalid PDF content type");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 50MB limit");
        }
        try (var in = file.getInputStream()) {
            byte[] magic = in.readNBytes(5);
            if (magic.length != 5 || magic[0] != '%' || magic[1] != 'P' || magic[2] != 'D' || magic[3] != 'F' || magic[4] != '-') {
                throw new IllegalArgumentException("File content is not a valid PDF");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to validate PDF file", e);
        }

        Path staging = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try { Files.createDirectories(staging); } catch (IOException e) { throw new IllegalStateException("Unable to prepare PDF staging", e); }
        PdfImportJob job = jobRepo.save(new PdfImportJob(originalFilename, null, file.getSize()));
        job.setStatus("QUEUED");
        job.setDefaultBrandId(validateDefaultBrand(defaultBrandId));
        job.setDefaultCategoryId(validateDefaultCategory(defaultCategoryId));
        job = jobRepo.save(job);
        final Long jobId = job.getId();
        final Path pdfPath = staging.resolve("job_" + jobId + ".pdf").normalize();
        if (!staging.equals(pdfPath.getParent())) throw new IllegalStateException("Invalid PDF staging path");
        try {
            try (var in = file.getInputStream()) { byte[] magic = in.readNBytes(5); if (magic.length != 5 || !"%PDF-".equals(new String(magic, java.nio.charset.StandardCharsets.US_ASCII))) throw new IllegalArgumentException("uploaded file is not a PDF"); }
            file.transferTo(pdfPath);
            job.setFilePath(pdfPath.toString());
            job = jobRepo.save(job);
            final String queuedFileName = originalFilename;
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                pdfImportExecutor.execute(() -> {
                                    int claimed = jobRepo.claimQueuedForProcessing(jobId, java.time.OffsetDateTime.now());
                                    if (claimed != 1) return;
                                    PdfImportJob asyncJob = jobRepo.findById(jobId).orElse(null);
                                    if (asyncJob == null) return;
                                    try { processPdfFile(asyncJob, pdfPath.toFile(), queuedFileName); }
                                    catch (Exception e) { if (!isOptimisticLockFailure(e)) { asyncJob.setStatus("FAILED"); asyncJob.setErrorMessage("Processing failed. The PDF could not be processed safely."); jobRepo.save(asyncJob); cleanupJobStagingImages(jobId); } }
                                    finally { try { Files.deleteIfExists(pdfPath); } catch (IOException ignored) {} }
                                });
                            } catch (RuntimeException rejected) {
                                PdfImportJob failed = jobRepo.findById(jobId).orElse(null);
                                if (failed != null) {
                                    failed.setStatus("FAILED");
                                    failed.setErrorMessage("PDF import worker queue is temporarily unavailable. Please upload again.");
                                    jobRepo.save(failed);
                                }
                                try { Files.deleteIfExists(pdfPath); } catch (IOException ignored) {}
                            }
                        }
                    });
        } catch (Exception e) {
            job.setStatus("FAILED"); job.setErrorMessage("Unable to queue PDF safely."); jobRepo.save(job);
            try { Files.deleteIfExists(pdfPath); } catch (IOException ignored) { }
            cleanupJobStagingImages(jobId);
        }
        return job;
    }

    private boolean isOptimisticLockFailure(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof jakarta.persistence.OptimisticLockException) return true;
            current = current.getCause();
        }
        return false;
    }

    public void recoverInterruptedJobs() {
        var cutoff = java.time.OffsetDateTime.now().minusMinutes(30);
        for (PdfImportJob job : jobRepo.findByStatusInAndUpdatedAtBefore(java.util.List.of("QUEUED", "PROCESSING", "EXTRACTING"), cutoff)) {
            String rawPath = job.getFilePath();
            if (rawPath == null || rawPath.isBlank()) {
                job.setStatus("FAILED"); job.setErrorMessage("Interrupted PDF job has no staging file."); jobRepo.save(job); continue;
            }
            Path path = Paths.get(rawPath).toAbsolutePath().normalize();
            Path root = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
            if (!path.startsWith(root) || !Files.isRegularFile(path)) {
                job.setStatus("FAILED"); job.setErrorMessage("Interrupted PDF staging file is unavailable."); jobRepo.save(job); continue;
            }
            job.setStatus("PROCESSING"); jobRepo.save(job);
            pdfImportExecutor.execute(() -> {
                try { processPdfFile(job, path.toFile(), job.getFileName()); }
                catch (Exception ex) { if (!isOptimisticLockFailure(ex)) { job.setStatus("FAILED"); job.setErrorMessage("Processing failed. The PDF could not be processed safely."); jobRepo.save(job); cleanupJobStagingImages(job.getId()); } }
                finally { try { Files.deleteIfExists(path); } catch (IOException ignored) {} }
            });
        }
    }

    private Long validateDefaultBrand(Long id) {
        if (id == null) return null;
        return brandRepo.findById(id).filter(b -> b.isActive()).map(com.wolfe.catalog.brand.Brand::getId)
                .orElseThrow(() -> new IllegalArgumentException("Default brand not found or inactive"));
    }

    private Long validateDefaultCategory(Long id) {
        if (id == null) return null;
        return categoryRepo.findById(id).filter(c -> c.isActive()).map(com.wolfe.catalog.adminmodel.ProductCategory::getId)
                .orElseThrow(() -> new IllegalArgumentException("Default category not found or inactive"));
    }

    private void applyImportDefaults(PdfImportJob job, List<PdfImportItem> items) {
        String brand = job.getDefaultBrandId() == null ? null : brandRepo.findById(job.getDefaultBrandId()).map(com.wolfe.catalog.brand.Brand::getName).orElse(null);
        String category = job.getDefaultCategoryId() == null ? null : categoryRepo.findById(job.getDefaultCategoryId()).map(com.wolfe.catalog.adminmodel.ProductCategory::getName).orElse(null);
        if (brand == null && category == null) return;
        for (PdfImportItem item : items) {
            if (brand != null && (item.getBrand() == null || item.getBrand().isBlank())) item.setBrand(brand);
            if (category != null && (item.getCategory() == null || item.getCategory().isBlank())) item.setCategory(category);
        }
    }

    private void processPdfFile(PdfImportJob job, File pdfFile, String originalFilename) {
        try (PDDocument document = Loader.loadPDF(pdfFile, IOUtils.createTempFileOnlyStreamCache())) {
            if (document.isEncrypted()) {
                job.setStatus("FAILED");
                job.setErrorMessage("Password-protected or encrypted PDFs are not supported without decryption key.");
                jobRepo.save(job);
                return;
            }

            int totalPages = document.getNumberOfPages();
            if (totalPages <= 0 || totalPages > MAX_PAGES) {
                job.setStatus("FAILED");
                job.setErrorMessage("PDF page count must be between 1 and " + MAX_PAGES);
                jobRepo.save(job);
                return;
            }
            job.setTotalPages(totalPages);
            job.setStatus("EXTRACTING");
            jobRepo.save(job);

            File importDir = new File(UPLOAD_DIR);
            if (!importDir.exists()) {
                importDir.mkdirs();
            }

            PDFTextStripper stripper = new PDFTextStripper();
            List<PdfImportItem> extractedItems = new ArrayList<>();
            long lastHeartbeatNanos = System.nanoTime();

            for (int pageIdx = 0; pageIdx < totalPages; pageIdx++) {
                int pageNum = pageIdx + 1;
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String pageText = stripper.getText(document);

                // Extract embedded images from page
                PDPage page = document.getPage(pageIdx);
                List<String> pageImages = extractImagesFromPage(page, job.getId(), pageNum);

                // Parse products from page
                List<PdfImportItem> itemsFromPage = parseProductsFromPageText(pageText, pageNum, pageImages, job.getId());
                extractedItems.addAll(itemsFromPage);

                // Keep the recovery scheduler from mistaking a long-running healthy worker
                // for a crashed job. The scheduler treats updated_at as the liveness signal,
                // so refresh it periodically while processing large/slow PDFs.
                if (java.util.concurrent.TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - lastHeartbeatNanos) >= 5) {
                    job.heartbeat();
                    jobRepo.save(job);
                    lastHeartbeatNanos = System.nanoTime();
                }
            }

            applyImportDefaults(job, extractedItems);

            // Duplicate detection
            for (PdfImportItem item : extractedItems) {
                if (item.getSku() != null && !item.getSku().isBlank()) {
                    Optional<ProductVariant> existingVariant = variantRepo.findBySku(item.getSku());
                    if (existingVariant.isPresent()) {
                        item.setDuplicateProductId(existingVariant.get().getProduct().getId());
                        item.setStatus("REVIEW_REQUIRED");
                    }
                }
                String slug = generateSlug(item.getName());
                Optional<Product> existingProduct = productRepo.findBySlug(slug);
                if (existingProduct.isPresent()) {
                    item.setDuplicateProductId(existingProduct.get().getId());
                    item.setStatus("REVIEW_REQUIRED");
                }
            }

            itemRepo.saveAll(extractedItems);
            job.setTotalExtracted(extractedItems.size());
            job.setStatus(extractedItems.isEmpty() ? "COMPLETED" : "REVIEW_REQUIRED");
            jobRepo.save(job);

        } catch (IOException e) {
            job.setStatus("FAILED");
            job.setErrorMessage("Failed to read PDF document safely.");
            jobRepo.save(job);
        }
    }

    private List<String> extractImagesFromPage(PDPage page, Long jobId, int pageNum) {
        List<String> images = new ArrayList<>();
        PDResources resources = page.getResources();
        if (resources == null) return images;

        int imgIndex = 1;
        for (COSName name : resources.getXObjectNames()) {
            try {
                if (resources.isImageXObject(name)) {
                    PDImageXObject imageObj = (PDImageXObject) resources.getXObject(name);
                    // Filter out tiny icons / separator lines (minimum 80x80)
                    if (images.size() >= MAX_IMAGES_PER_PAGE) {
                        break;
                    }
                    long pixels = (long) imageObj.getWidth() * imageObj.getHeight();
                    if (imageObj.getWidth() >= 80 && imageObj.getHeight() >= 80 && pixels <= MAX_IMAGE_PIXELS) {
                        BufferedImage bImage = imageObj.getImage();
                        if (bImage != null) {
                            String filename = "import_" + jobId + "_p" + pageNum + "_" + imgIndex + ".jpg";
                            File outputFile = new File(UPLOAD_DIR, filename);
                            ImageIO.write(bImage, "JPEG", outputFile);
                            images.add("/api/v1/admin/pdf-imports/media/" + filename);
                            imgIndex++;
                        }
                    }
                }
            } catch (Exception e) {
                // A single malformed embedded image must not abort the whole import.
            }
        }
        return images;
    }

    private List<PdfImportItem> parseProductsFromPageText(String text, int pageNum, List<String> pageImages, Long jobId) {
        List<PdfImportItem> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            if (!pageImages.isEmpty()) {
                // If scanned or image only, create a draft item with the image
                result.add(new PdfImportItem(
                        jobId, pageNum, "PDF page " + pageNum + " — review required", null,
                        null, null, null, null,
                        null, null, null, null, null,
                        "Image-only page extraction; product details require manual verification before approval.",
                        pageImages.get(0),
                        pageImages.size() > 1 ? String.join(",", pageImages) : "",
                        "[]", "{}", "REVIEW_REQUIRED", new BigDecimal("0.70"), null, "Image-only page extraction; manual review recommended."
                ));
            }
            return result;
        }

        String[] lines = text.split("\\r?\\n");
        List<String> paragraphs = new ArrayList<>();
        StringBuilder currentPara = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                if (currentPara.length() > 0) {
                    paragraphs.add(currentPara.toString());
                    currentPara = new StringBuilder();
                }
            } else {
                if (currentPara.length() > 0) currentPara.append(" ");
                currentPara.append(trimmed);
            }
        }
        if (currentPara.length() > 0) paragraphs.add(currentPara.toString());

        int imgIdx = 0;
        for (String para : paragraphs) {
            if (para.length() < 10) continue;

            String name = extractName(para);
            if (name == null || name.length() < 3) continue;

            String sku = extractSku(para);
            String brand = extractBrand(para);
            String category = extractCategory(para);
            String subcategory = extractSubcategory(para, category);
            BigDecimal price = extractPrice(para);
            String finish = extractFinish(para);
            String material = extractMaterial(para);
            String color = extractColor(para, finish);
            String size = extractSize(para);
            String dimensions = extractDimensions(para);

            String mainImage = pageImages.size() > imgIdx ? pageImages.get(imgIdx) : null;
            imgIdx++;

            BigDecimal confidence = new BigDecimal("0.90");
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                confidence = confidence.subtract(new BigDecimal("0.20"));
            }

            result.add(new PdfImportItem(
                    jobId, pageNum, name, sku, brand, category, subcategory, price,
                    finish, material, color, size, dimensions, para,
                    mainImage, "", "[]", "{}",
                    confidence.compareTo(new BigDecimal("0.85")) >= 0 ? "DRAFT" : "REVIEW_REQUIRED",
                    confidence, null, null
            ));
        }

        if (result.isEmpty() && !pageImages.isEmpty()) {
            result.add(new PdfImportItem(
                    jobId, pageNum, "PDF page " + pageNum + " — review required", null,
                    null, null, null, null,
                    null, null, null, null, null,
                    text.length() > 500 ? text.substring(0, 500) : text,
                    pageImages.get(0), "", "[]", "{}",
                    "REVIEW_REQUIRED", new BigDecimal("0.75"), null, "Requires detail verification"
            ));
        }

        return result;
    }

    private String extractName(String text) {
        String clean = text.replaceAll("(?i)(SKU|CODE|ART|PRICE|MRP|RS|₹|INR):?.*$", "").trim();
        if (clean.length() > 60) {
            String[] words = clean.split(" ");
            if (words.length > 7) {
                return String.join(" ", Arrays.copyOfRange(words, 0, 7));
            }
        }
        return clean.isEmpty() ? null : clean;
    }

    private String extractSku(String text) {
        Pattern pattern = Pattern.compile("(?i)(?:SKU|CODE|ART(?:\\. ?NO)?|MODEL)\\s*[:#]?\\s*([A-Z0-9\\-_/]{3,20})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return null;
    }

    private String extractBrand(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("hettich")) return "Hettich";
        if (lower.contains("hafele") || lower.contains("häfele")) return "Hafele";
        if (lower.contains("blum")) return "Blum";
        if (lower.contains("greenply")) return "Greenply";
        if (lower.contains("centuryply") || lower.contains("century ply")) return "CenturyPly";
        if (lower.contains("merino")) return "Merino";
        if (lower.contains("greenlam")) return "Greenlam";
        if (lower.contains("godrej")) return "Godrej";
        if (lower.contains("ebco")) return "Ebco";
        return null;
    }

    private String extractCategory(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("plywood") || lower.contains("bwp") || lower.contains("marine ply") || lower.contains("calibrated")) {
            return "Plywood";
        }
        if (lower.contains("laminate") || lower.contains("sunmica") || lower.contains("mica") || lower.contains("gloss") || lower.contains("suede") || lower.contains("fluted oak")) {
            return "Laminates";
        }
        if (lower.contains("kitchen") || lower.contains("drawer") || lower.contains("tandem") || lower.contains("channel") || lower.contains("magic corner") || lower.contains("basket") || lower.contains("cutlery") || lower.contains("waste") || lower.contains("pullout") || lower.contains("lift-up")) {
            return "Kitchen Accessories";
        }
        return null;
    }

    private String extractSubcategory(String text, String category) {
        String lower = text.toLowerCase();
        if ("Plywood".equals(category)) {
            if (lower.contains("marine") || lower.contains("bwp") || lower.contains("waterproof")) return "Marine / BWP Ply";
            if (lower.contains("calibrated")) return "Calibrated Ply";
            if (lower.contains("flexi") || lower.contains("flexible")) return "Flexible Ply";
            if (lower.contains("fire")) return "Fire Retardant Ply";
            return "Commercial Ply";
        }
        if ("Laminates".equals(category)) {
            if (lower.contains("gloss")) return "High Gloss";
            if (lower.contains("matte") || lower.contains("suede")) return "Matte / Suede";
            if (lower.contains("fluted") || lower.contains("texture")) return "Textured & Fluted";
            if (lower.contains("wood") || lower.contains("oak") || lower.contains("walnut")) return "Wood Grain";
            if (lower.contains("metal") || lower.contains("acrylic") || lower.contains("brass")) return "Metallic & Acrylic";
            return "Matte / Suede";
        }
        if ("Kitchen Accessories".equals(category)) {
            if (lower.contains("drawer") || lower.contains("channel") || lower.contains("tandem")) return "Drawer & Sliding Systems";
            if (lower.contains("hinge")) return "Cabinet Hinges";
            if (lower.contains("lift") || lower.contains("flap") || lower.contains("stay")) return "Flap & Lift-up Fittings";
            if (lower.contains("corner") || lower.contains("carousel") || lower.contains("pantry")) return "Corner & Storage Pull-outs";
            if (lower.contains("basket") || lower.contains("tray") || lower.contains("organizer")) return "Baskets & Organizers";
            if (lower.contains("waste") || lower.contains("bin")) return "Waste Management";
            return "Drawer & Sliding Systems";
        }
        if (lower.contains("knob")) return "Knobs";
        if (lower.contains("hook")) return "Hooks";
        if (lower.contains("lock")) return "Locks";
        if (lower.contains("hinge")) return "Hinges";
        return "Handles";
    }

    private BigDecimal extractPrice(String text) {
        Pattern pattern = Pattern.compile("(?:₹|Rs\\.?|INR)\\s*([0-9,]+(?:\\.[0-9]{2})?)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String clean = matcher.group(1).replace(",", "");
            try {
                return new BigDecimal(clean);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String extractFinish(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("brushed brass")) return "Brushed Brass";
        if (lower.contains("antique brass")) return "Antique Brass";
        if (lower.contains("matte black") || lower.contains("matt black")) return "Matte Black";
        if (lower.contains("satin nickel")) return "Satin Nickel";
        if (lower.contains("polished chrome") || lower.contains("chrome")) return "Polished Chrome";
        if (lower.contains("bronze") || lower.contains("oil rubbed")) return "Bronze";
        if (lower.contains("stainless") || lower.contains("ss 304")) return "Stainless Steel";
        return null;
    }

    private String extractMaterial(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("brass") || lower.contains("solid brass")) return "Brass";
        if (lower.contains("stainless steel") || lower.contains("steel") || lower.contains("ss")) return "Stainless Steel";
        if (lower.contains("zinc") || lower.contains("zamak")) return "Zinc Alloy";
        if (lower.contains("hardwood") || lower.contains("gurjan") || lower.contains("eucalyptus")) return "Hardwood";
        if (lower.contains("kraft paper") || lower.contains("resin")) return "High Pressure Laminate";
        return null;
    }

    private String extractColor(String text, String finish) {
        String lower = text.toLowerCase();
        if (lower.contains("brass") || lower.contains("gold")) return "Brass";
        if (lower.contains("black")) return "Black";
        if (lower.contains("silver") || lower.contains("chrome") || lower.contains("nickel")) return "Silver";
        if (lower.contains("bronze") || lower.contains("brown")) return "Bronze";
        if (lower.contains("white")) return "White";
        if (lower.contains("grey") || lower.contains("anthracite")) return "Grey";
        return null;
    }

    private String extractSize(String text) {
        Pattern pattern = Pattern.compile("([0-9]+(?:\\.[0-9]+)?\\s*(?:mm|inch|cm|ft|x\\s*[0-9]+))");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractDimensions(String text) {
        Pattern pattern = Pattern.compile("([0-9]+\\s*(?:mm|cm|ft)?\\s*[xX*×]\\s*[0-9]+(?:\\s*[xX*×]\\s*[0-9]+)?\\s*(?:mm|cm|ft)?)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Transactional
    public Product approveItem(Long itemId) {
        PdfImportItem item = itemRepo.findByIdForUpdate(itemId).orElseThrow(() -> new IllegalArgumentException("Import item not found"));
        if ("REJECTED".equalsIgnoreCase(item.getStatus()) || "APPROVED".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalStateException("Import item cannot be approved from status: " + item.getStatus());
        }
        if (item.getName() == null || item.getName().isBlank() || item.getName().startsWith("PDF page "))
            throw new IllegalArgumentException("Product name must be explicitly verified before approval");
        if (item.getSku() == null || item.getSku().isBlank())
            throw new IllegalArgumentException("SKU/product code must be explicitly verified before approval");
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Product price must be explicitly verified before approval");
        if (item.getBrand() == null || item.getBrand().isBlank() || item.getCategory() == null || item.getCategory().isBlank()
                || item.getFinish() == null || item.getFinish().isBlank()
                || item.getMaterial() == null || item.getMaterial().isBlank()
                || item.getColor() == null || item.getColor().isBlank())
            throw new IllegalArgumentException("Brand, category, finish, material, and color must be explicitly verified before approval");

        String publishedImageUrl = publishImage(item.getImageUrl(), item.getId());
        String publishedMediaUrls = publishMediaUrls(item.getMediaUrls(), item.getId());
        String slug = generateSlug(item.getName());
        Brand brand = brandRepo.findBySlug(generateSlug(item.getBrand())).orElseGet(() -> {
            return brandRepo.save(new Brand(item.getBrand(), generateSlug(item.getBrand()), null, null, null, true, 50));
        });

        Product product = productRepo.findBySlug(slug).orElseGet(() -> {
            Product p = new Product(slug, item.getName(), item.getPrice(),
                    item.getCategory(), item.getFinish(), item.getDescription());
            p.setBrandId(brand.getId());
            p.setBrandName(brand.getName());
            p.setSubcategory(item.getSubcategory());
            p.setMaterial(item.getMaterial());
            p.setColor(item.getColor());
            p.setDimensions(item.getDimensions());
            p.setImageUrl(publishedImageUrl);
            p.setMediaUrls(publishedMediaUrls);
            p.setAttributesJson(item.getAttributesJson());
            p.setActive(true);
            return productRepo.save(p);
        });

        // Add default variant
        String varSku = item.getSku().trim().toUpperCase(Locale.ROOT);
        if (variantRepo.findBySku(varSku).isEmpty()) {
            ProductVariant variant = new ProductVariant(
                    product, item.getName(), varSku, item.getColor(), item.getMaterial(),
                    item.getSize(), item.getFinish(),
                    item.getDimensions(), item.getPrice(), 0, publishedImageUrl, item.getAttributesJson(),
                    true, 0
            );
            variantRepo.save(variant);
        }

        inventoryRepo.insertDefault(product.getId(), 0);

        item.setImageUrl(publishedImageUrl);
        item.setMediaUrls(publishedMediaUrls);
        item.setStatus("APPROVED");
        itemRepo.save(item);
        cleanupItemStagingImages(item);
        updateJobStatusIfResolved(item.getJobId());

        return product;
    }

    @Transactional
    public List<Product> approveAllInJob(Long jobId) {
        List<PdfImportItem> items = itemRepo.findByJobId(jobId);
        List<Product> approved = new ArrayList<>();
        for (PdfImportItem item : items) {
            if ("DRAFT".equals(item.getStatus()) || "REVIEW_REQUIRED".equals(item.getStatus()) || "IMPORTED".equals(item.getStatus())) {
                approved.add(approveItem(item.getId()));
            }
        }
        PdfImportJob job = jobRepo.findById(jobId).orElse(null);
        if (job != null) {
            boolean unresolved = itemRepo.findByJobId(jobId).stream().anyMatch(i ->
                    "DRAFT".equalsIgnoreCase(i.getStatus()) || "REVIEW_REQUIRED".equalsIgnoreCase(i.getStatus()) || "IMPORTED".equalsIgnoreCase(i.getStatus()));
            job.setStatus(unresolved ? "REVIEW_REQUIRED" : "COMPLETED");
            jobRepo.save(job);
        }
        return approved;
    }

    @Transactional
    public PdfImportItem rejectItem(Long itemId) {
        PdfImportItem item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Import item not found"));
        item.setStatus("REJECTED");
        PdfImportItem saved = itemRepo.save(item);
        cleanupItemStagingImages(saved);
        updateJobStatusIfResolved(saved.getJobId());
        return saved;
    }

    @Transactional
    public PdfImportItem updateItem(Long itemId, PdfImportItemUpdateRequest updated) {
        PdfImportItem item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Import item not found"));
        item.updateDetails(
                updated.name(), updated.sku(), updated.brand(), updated.category(),
                updated.subcategory(), updated.price(), updated.finish(), updated.material(),
                updated.color(), updated.size(), updated.dimensions(), updated.description(),
                validateMediaReference(updated.imageUrl()), validateMediaUrls(updated.mediaUrls()),
                updated.extractedVariantsJson(), updated.attributesJson(), updated.notes()
        );
        if ("REVIEW_REQUIRED".equalsIgnoreCase(item.getStatus()) || "DRAFT".equalsIgnoreCase(item.getStatus()) || "IMPORTED".equalsIgnoreCase(item.getStatus())) {
            item.setStatus("DRAFT");
        }
        PdfImportItem saved = itemRepo.save(item);
        jobRepo.findById(saved.getJobId()).ifPresent(job -> {
            job.setStatus("REVIEW_REQUIRED");
            jobRepo.save(job);
        });
        return saved;
    }

    private String validateMediaReference(String url) {
        if (url == null || url.isBlank()) return url;
        String v = url.trim();
        if (v.startsWith("http://") || v.startsWith("https://") || v.startsWith("//")) {
            throw new IllegalArgumentException("External media URLs are not permitted");
        }
        if (!(v.startsWith("/catalog/imports/") || v.startsWith("/catalog/published/") || v.startsWith("/api/v1/admin/pdf-imports/media/"))) {
            throw new IllegalArgumentException("Invalid media URL");
        }
        return v;
    }

    private String validateMediaUrls(String urls) {
        if (urls == null || urls.isBlank()) return urls;
        return Arrays.stream(urls.split(",")).map(String::trim).filter(s -> !s.isBlank())
                .map(this::validateMediaReference).collect(java.util.stream.Collectors.joining(","));
    }

    private String publishImage(String url, Long itemId) {
        if (url == null || url.isBlank()) return null;
        return publishOne(url, itemId);
    }

    private String publishMediaUrls(String urls, Long itemId) {
        if (urls == null || urls.isBlank()) return urls;
        return Arrays.stream(urls.split(","))
                .map(String::trim).filter(s -> !s.isBlank())
                .map(s -> publishOne(s, itemId))
                .collect(java.util.stream.Collectors.joining(","));
    }

    private String publishOne(String url, Long itemId) {
        if (url == null) return null;
        String prefix = "/api/v1/admin/pdf-imports/media/";
        if (!url.startsWith(prefix) && !url.startsWith("/catalog/imports/")) return url;
        String filename = Paths.get(url.substring(url.startsWith(prefix) ? prefix.length() : "/catalog/imports/".length())).getFileName().toString();
        Path source = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
        Path root = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        if (!source.toAbsolutePath().normalize().startsWith(root)) throw new IllegalArgumentException("Invalid import media path");
        try {
            Path publishedRoot = Paths.get(PUBLISHED_DIR).toAbsolutePath().normalize();
            Files.createDirectories(publishedRoot);
            String safeName = "item_" + itemId + "_" + filename;
            Path target = publishedRoot.resolve(safeName).normalize();
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return "/catalog/published/" + safeName;
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to publish approved catalog image", ex);
        }
    }


    private void cleanupItemStagingImages(PdfImportItem item) {
        deleteStagingUrl(item.getImageUrl());
        if (item.getMediaUrls() != null && !item.getMediaUrls().isBlank()) {
            for (String url : item.getMediaUrls().split(",")) deleteStagingUrl(url.trim());
        }
    }

    private void deleteStagingUrl(String url) {
        if (url == null) return;
        String prefix = "/api/v1/admin/pdf-imports/media/";
        if (!url.startsWith(prefix) && !url.startsWith("/catalog/imports/")) return;
        String filename = Paths.get(url.substring(url.startsWith(prefix) ? prefix.length() : "/catalog/imports/".length())).getFileName().toString();
        Path root = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Path source = root.resolve(filename).normalize();
        if (!source.startsWith(root)) return;
        try { Files.deleteIfExists(source); } catch (IOException ignored) { }
    }

    private void updateJobStatusIfResolved(Long jobId) {
        jobRepo.findById(jobId).ifPresent(job -> {
            boolean unresolved = itemRepo.findByJobId(jobId).stream().anyMatch(i ->
                    "DRAFT".equalsIgnoreCase(i.getStatus()) ||
                    "REVIEW_REQUIRED".equalsIgnoreCase(i.getStatus()) ||
                    "IMPORTED".equalsIgnoreCase(i.getStatus()));
            job.setStatus(unresolved ? "REVIEW_REQUIRED" : "COMPLETED");
            jobRepo.save(job);
        });
    }

    private void cleanupJobStagingImages(Long jobId) {
        Path root = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try (var stream = Files.list(root)) {
            stream.filter(p -> p.getFileName().toString().startsWith("import_" + jobId + "_"))
                    .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) { } });
        } catch (IOException ignored) { }
    }

    private String generateSlug(String name) {
        if (name == null || name.isBlank()) return "product-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String normalized = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFKD)
                .replaceAll("[^\\p{ASCII}]", "");
        String slug = normalized.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        return slug.isBlank() ? "product-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) : slug;
    }
}
