package com.wolfe.catalog.pdf;

import com.wolfe.catalog.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/pdf-imports")
@PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<PdfImportJob> upload(@RequestParam("file") MultipartFile file) {
        PdfImportJob job = service.uploadAndProcess(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }

    @GetMapping
    public List<PdfImportJob> listJobs() {
        return jobRepo.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<PdfImportJob> getJob(@PathVariable Long jobId) {
        return jobRepo.findById(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{jobId}/items")
    public List<PdfImportItem> getJobItems(@PathVariable Long jobId) {
        return itemRepo.findByJobIdOrderByPageNumberAscIdAsc(jobId);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<PdfImportItem> updateItem(@PathVariable Long itemId, @RequestBody PdfImportItem body) {
        return ResponseEntity.ok(service.updateItem(itemId, body));
    }

    @PostMapping("/items/{itemId}/approve")
    public ResponseEntity<Product> approveItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.approveItem(itemId));
    }

    @PostMapping("/items/{itemId}/reject")
    public ResponseEntity<PdfImportItem> rejectItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(service.rejectItem(itemId));
    }

    @PostMapping("/{jobId}/approve-all")
    public ResponseEntity<List<Product>> approveAll(@PathVariable Long jobId) {
        return ResponseEntity.ok(service.approveAllInJob(jobId));
    }
}
