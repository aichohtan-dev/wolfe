package com.wolfe.catalog.pdf;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.catalog.brand.Brand;
import com.wolfe.catalog.brand.BrandRepository;
import com.wolfe.inventory.InventoryRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
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
    private final PdfImportJobRepository jobRepo;
    private final PdfImportItemRepository itemRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final BrandRepository brandRepo;
    private final InventoryRepository inventoryRepo;

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String UPLOAD_DIR = "public/catalog/imports";

    public PdfImportService(PdfImportJobRepository jobRepo, PdfImportItemRepository itemRepo,
                            ProductRepository productRepo, ProductVariantRepository variantRepo,
                            BrandRepository brandRepo, InventoryRepository inventoryRepo) {
        this.jobRepo = jobRepo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
        this.brandRepo = brandRepo;
        this.inventoryRepo = inventoryRepo;
    }

    @Transactional
    public PdfImportJob uploadAndProcess(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "catalog.pdf";
        if (!originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files (.pdf) are supported");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 50MB limit");
        }

        PdfImportJob job = jobRepo.save(new PdfImportJob(originalFilename, null, file.getSize()));
        job.setStatus("PROCESSING");
        jobRepo.save(job);

        try {
            byte[] bytes = file.getBytes();
            processPdfBytes(job, bytes, originalFilename);
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage("Processing failed: " + e.getMessage());
            jobRepo.save(job);
        }

        return job;
    }

    private void processPdfBytes(PdfImportJob job, byte[] bytes, String originalFilename) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) {
                job.setStatus("FAILED");
                job.setErrorMessage("Password-protected or encrypted PDFs are not supported without decryption key.");
                jobRepo.save(job);
                return;
            }

            int totalPages = document.getNumberOfPages();
            job.setTotalPages(totalPages);
            job.setStatus("EXTRACTING");
            jobRepo.save(job);

            File importDir = new File(UPLOAD_DIR);
            if (!importDir.exists()) {
                importDir.mkdirs();
            }

            PDFTextStripper stripper = new PDFTextStripper();
            List<PdfImportItem> extractedItems = new ArrayList<>();

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
            }

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
            job.setErrorMessage("Failed to read PDF document: " + e.getMessage());
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
                    if (imageObj.getWidth() >= 80 && imageObj.getHeight() >= 80) {
                        BufferedImage bImage = imageObj.getImage();
                        if (bImage != null) {
                            String filename = "import_" + jobId + "_p" + pageNum + "_" + imgIndex + ".jpg";
                            File outputFile = new File(UPLOAD_DIR, filename);
                            ImageIO.write(bImage, "JPEG", outputFile);
                            images.add("/catalog/imports/" + filename);
                            imgIndex++;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        return images;
    }

    private List<PdfImportItem> parseProductsFromPageText(String text, int pageNum, List<String> pageImages, Long jobId) {
        List<PdfImportItem> result = new ArrayList<>();
        if (text == null || text.isBlank()) {
            if (!pageImages.isEmpty()) {
                // If scanned or image only, create a draft item with the image
                result.add(new PdfImportItem(
                        jobId, pageNum, "Extracted Item Page " + pageNum, "EXT-P" + pageNum + "-01",
                        "Wolfe Heritage", "Hardware", "Handles", new BigDecimal("4500.00"),
                        "Brushed Brass", "Brass", "Brass", "Standard", "150mm",
                        "Extracted product from catalog page " + pageNum,
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

            String sku = extractSku(para, pageNum, result.size() + 1);
            String brand = extractBrand(para);
            String category = extractCategory(para);
            String subcategory = extractSubcategory(para, category);
            BigDecimal price = extractPrice(para);
            String finish = extractFinish(para);
            String material = extractMaterial(para);
            String color = extractColor(para, finish);
            String size = extractSize(para);
            String dimensions = extractDimensions(para);

            String mainImage = pageImages.size() > imgIdx ? pageImages.get(imgIdx) : "/catalog/brass-01.jpg";
            imgIdx++;

            BigDecimal confidence = new BigDecimal("0.90");
            if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
                price = new BigDecimal("4500.00");
                confidence = confidence.subtract(new BigDecimal("0.10"));
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
                    jobId, pageNum, "Extracted Product Page " + pageNum, "EXT-P" + pageNum + "-01",
                    "Wolfe Heritage", "Hardware", "Handles", new BigDecimal("4500.00"),
                    "Brushed Brass", "Brass", "Brass", "Standard", "150mm",
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
        return clean.isEmpty() ? "Architectural Component" : clean;
    }

    private String extractSku(String text, int page, int index) {
        Pattern pattern = Pattern.compile("(?i)(?:SKU|CODE|ART(?:\\. ?NO)?|MODEL)\\s*[:#]?\\s*([A-Z0-9\\-_/]{3,20})");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return "WLF-PDF-P" + page + "-" + String.format("%02d", index);
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
        return "Wolfe Heritage";
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
        return "Hardware";
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
        return "Brushed Brass";
    }

    private String extractMaterial(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("brass") || lower.contains("solid brass")) return "Brass";
        if (lower.contains("stainless steel") || lower.contains("steel") || lower.contains("ss")) return "Stainless Steel";
        if (lower.contains("zinc") || lower.contains("zamak")) return "Zinc Alloy";
        if (lower.contains("hardwood") || lower.contains("gurjan") || lower.contains("eucalyptus")) return "Hardwood";
        if (lower.contains("kraft paper") || lower.contains("resin")) return "High Pressure Laminate";
        return "Solid Brass";
    }

    private String extractColor(String text, String finish) {
        String lower = text.toLowerCase();
        if (lower.contains("brass") || lower.contains("gold")) return "Brass";
        if (lower.contains("black")) return "Black";
        if (lower.contains("silver") || lower.contains("chrome") || lower.contains("nickel")) return "Silver";
        if (lower.contains("bronze") || lower.contains("brown")) return "Bronze";
        if (lower.contains("white")) return "White";
        if (lower.contains("grey") || lower.contains("anthracite")) return "Grey";
        return "Brass";
    }

    private String extractSize(String text) {
        Pattern pattern = Pattern.compile("([0-9]+(?:\\.[0-9]+)?\\s*(?:mm|inch|cm|ft|x\\s*[0-9]+))");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Standard";
    }

    private String extractDimensions(String text) {
        Pattern pattern = Pattern.compile("([0-9]+\\s*(?:mm|cm|ft)?\\s*[xX*×]\\s*[0-9]+(?:\\s*[xX*×]\\s*[0-9]+)?\\s*(?:mm|cm|ft)?)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Standard";
    }

    @Transactional
    public Product approveItem(Long itemId) {
        PdfImportItem item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Import item not found"));

        String slug = generateSlug(item.getName());
        Brand brand = brandRepo.findBySlug(generateSlug(item.getBrand())).orElseGet(() -> {
            return brandRepo.save(new Brand(item.getBrand(), generateSlug(item.getBrand()), null, null, null, true, 50));
        });

        Product product = productRepo.findBySlug(slug).orElseGet(() -> {
            Product p = new Product(slug, item.getName(), item.getPrice() != null ? item.getPrice() : new BigDecimal("4500.00"),
                    item.getCategory(), item.getFinish(), item.getDescription());
            p.setBrandId(brand.getId());
            p.setBrandName(brand.getName());
            p.setSubcategory(item.getSubcategory());
            p.setMaterial(item.getMaterial());
            p.setColor(item.getColor());
            p.setDimensions(item.getDimensions());
            p.setImageUrl(item.getImageUrl());
            p.setMediaUrls(item.getMediaUrls());
            p.setAttributesJson(item.getAttributesJson());
            p.setActive(true);
            return productRepo.save(p);
        });

        // Add default variant
        String varSku = item.getSku() != null && !item.getSku().isBlank() ? item.getSku() : slug + "-VAR-1";
        if (variantRepo.findBySku(varSku).isEmpty()) {
            ProductVariant variant = new ProductVariant(
                    product, item.getName(), varSku, item.getColor(), item.getMaterial(),
                    item.getSize() != null ? item.getSize() : "Standard", item.getFinish(),
                    item.getDimensions(), item.getPrice(), 50, item.getImageUrl(), item.getAttributesJson(),
                    true, 0
            );
            variantRepo.save(variant);
        }

        inventoryRepo.insertDefault(product.getId(), 50);

        item.setStatus("APPROVED");
        itemRepo.save(item);

        return product;
    }

    @Transactional
    public List<Product> approveAllInJob(Long jobId) {
        List<PdfImportItem> items = itemRepo.findByJobId(jobId);
        List<Product> approved = new ArrayList<>();
        for (PdfImportItem item : items) {
            if ("DRAFT".equals(item.getStatus()) || "REVIEW_REQUIRED".equals(item.getStatus())) {
                approved.add(approveItem(item.getId()));
            }
        }
        PdfImportJob job = jobRepo.findById(jobId).orElse(null);
        if (job != null) {
            job.setStatus("COMPLETED");
            jobRepo.save(job);
        }
        return approved;
    }

    @Transactional
    public PdfImportItem rejectItem(Long itemId) {
        PdfImportItem item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Import item not found"));
        item.setStatus("REJECTED");
        return itemRepo.save(item);
    }

    @Transactional
    public PdfImportItem updateItem(Long itemId, PdfImportItem updated) {
        PdfImportItem item = itemRepo.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Import item not found"));
        item.updateDetails(
                updated.getName(), updated.getSku(), updated.getBrand(), updated.getCategory(),
                updated.getSubcategory(), updated.getPrice(), updated.getFinish(), updated.getMaterial(),
                updated.getColor(), updated.getSize(), updated.getDimensions(), updated.getDescription(),
                updated.getImageUrl(), updated.getMediaUrls(), updated.getExtractedVariantsJson(),
                updated.getAttributesJson(), updated.getNotes()
        );
        return itemRepo.save(item);
    }

    private String generateSlug(String name) {
        if (name == null || name.isBlank()) return "product-" + UUID.randomUUID().toString().substring(0, 8);
        return name.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
