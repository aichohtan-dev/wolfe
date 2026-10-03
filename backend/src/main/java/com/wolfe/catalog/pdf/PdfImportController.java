package com.wolfe.catalog.pdf;

import com.wolfe.catalog.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/pdf-imports")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class PdfImportController {
    private final PdfImportService service;
    private final PdfImportJobRepository jobRepo;
    private final PdfImportItemRepository itemRepo;

    public PdfImportController(PdfImportService service, PdfImportJobRepository jobRepo, PdfImportItemRepository itemRepo) {
        this.service = service;
        this.jobRepo = jobRepo;
        this.itemRepo = itemRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<PdfImportJobView> upload(@RequestParam("file") MultipartFile file,
                                               @RequestParam(required = false) Long brandId,
                                               @RequestParam(required = false) Long categoryId) {
        PdfImportJob job = service.uploadAndProcess(file, brandId, categoryId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new PdfImportJobView(job));
    }

    @GetMapping("/media/{filename:.+}")
    public ResponseEntity<Resource> media(@PathVariable String filename) {
        String safeName = Paths.get(filename).getFileName().toString();
        if (!safeName.equals(filename) || safeName.isBlank()) return ResponseEntity.badRequest().build();
        String lower = safeName.toLowerCase(java.util.Locale.ROOT);
        MediaType mediaType = switch (lower.substring(lower.lastIndexOf('.') + 1)) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            default -> null;
        };
        if (mediaType == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        try {
            Path root = Paths.get("public/catalog/imports").toAbsolutePath().normalize();
            Path file = root.resolve(safeName).normalize();
            if (!file.startsWith(root) || !Files.isRegularFile(file)) return ResponseEntity.notFound().build();
            Resource resource = new UrlResource(file.toUri());
            return ResponseEntity.ok().contentType(mediaType).header("Cache-Control", "private, no-store").body(resource);
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    public record PdfImportJobView(Long id, String fileName, Long fileSize, Long defaultBrandId, Long defaultCategoryId, String status, Integer totalPages, Integer totalExtracted, String errorMessage, java.time.OffsetDateTime createdAt, java.time.OffsetDateTime updatedAt) {
        PdfImportJobView(PdfImportJob j) { this(j.getId(), j.getFileName(), j.getFileSize(), j.getDefaultBrandId(), j.getDefaultCategoryId(), j.getStatus(), j.getTotalPages(), j.getTotalExtracted(), j.getErrorMessage(), j.getCreatedAt(), j.getUpdatedAt()); }
    }

    @GetMapping
    public List<PdfImportJobView> listJobs() {
        return jobRepo.findAll(org.springframework.data.domain.PageRequest.of(0, 100, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
                .stream().map(PdfImportJobView::new).toList();
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<PdfImportJobView> getJob(@PathVariable Long jobId) {
        return jobRepo.findById(jobId)
                .map(j -> ResponseEntity.ok(new PdfImportJobView(j)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{jobId}/items")
    public List<PdfImportItem> getJobItems(@PathVariable Long jobId) {
        return itemRepo.findByJobIdOrderByPageNumberAscIdAsc(jobId);
    }

    public record PdfImportItemUpdateRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 120) String sku,
            @Size(max = 120) String brand,
            @Size(max = 120) String category,
            @Size(max = 120) String subcategory,
            @DecimalMin("0") BigDecimal price,
            @Size(max = 100) String finish,
            @Size(max = 100) String material,
            @Size(max = 100) String color,
            @Size(max = 100) String size,
            @Size(max = 200) String dimensions,
            @Size(max = 2000) String description,
            @Size(max = 1000) String imageUrl,
            @Size(max = 4000) String mediaUrls,
            @Size(max = 8000) String extractedVariantsJson,
            @Size(max = 8000) String attributesJson,
            @Size(max = 1000) String notes) {}

    @PutMapping("/items/{itemId}")
    public ResponseEntity<PdfImportItem> updateItem(@PathVariable Long itemId, @Valid @RequestBody PdfImportItemUpdateRequest body) {
        return ResponseEntity.ok(service.updateItem(itemId, new PdfImportService.PdfImportItemUpdateRequest(body.name(), body.sku(), body.brand(), body.category(), body.subcategory(), body.price(), body.finish(), body.material(), body.color(), body.size(), body.dimensions(), body.description(), body.imageUrl(), body.mediaUrls(), body.extractedVariantsJson(), body.attributesJson(), body.notes())));
    }

    @PostMapping("/items/{itemId}/approve")
    public ResponseEntity<com.wolfe.catalog.ProductController.ProductPublicView> approveItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(new com.wolfe.catalog.ProductController.ProductPublicView(service.approveItem(itemId)));
    }

    @PostMapping("/items/{itemId}/reject")
    public ResponseEntity<PdfImportItem> rejectItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.rejectItem(itemId));
    }

    @PostMapping("/{jobId}/approve-all")
    public ResponseEntity<List<com.wolfe.catalog.ProductController.ProductPublicView>> approveAll(@PathVariable Long jobId) {
        return ResponseEntity.ok(service.approveAllInJob(jobId).stream().map(com.wolfe.catalog.ProductController.ProductPublicView::new).toList());
    }
}
