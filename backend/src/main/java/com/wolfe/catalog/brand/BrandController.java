package com.wolfe.catalog.brand;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class BrandController {
    private final BrandRepository repo;

    public BrandController(BrandRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/brands")
    public List<BrandResponse> listActive() {
        return repo.findByActiveTrueOrderBySortOrderAscNameAsc().stream().map(BrandResponse::from).toList();
    }

    @GetMapping("/brands/{slug}")
    public ResponseEntity<BrandResponse> getBySlug(@PathVariable String slug) {
        return repo.findBySlug(slug).filter(Brand::isActive)
                .map(b -> ResponseEntity.ok(BrandResponse.from(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/brands")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<BrandResponse> listAdmin() {
        return repo.findAllByOrderBySortOrderAscNameAsc().stream().map(BrandResponse::from).toList();
    }

    @PostMapping("/admin/brands")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<BrandResponse> create(@RequestBody BrandRequest req) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("Brand name is required");
        }
        String name = req.name().trim();
        String slug = req.slug() != null && !req.slug().isBlank() ? req.slug().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-") : name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        Brand brand = new Brand(name, slug, req.logoUrl(), req.description(), req.website(), req.active() == null || req.active(), req.sortOrder() == null ? 0 : req.sortOrder());
        return ResponseEntity.status(HttpStatus.CREATED).body(BrandResponse.from(repo.save(brand)));
    }

    @PutMapping("/admin/brands/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<BrandResponse> update(@PathVariable Long id, @RequestBody BrandRequest req) {
        Brand brand = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        String name = req.name() != null && !req.name().isBlank() ? req.name().trim() : brand.getName();
        String slug = req.slug() != null && !req.slug().isBlank() ? req.slug().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-") : brand.getSlug();
        brand.update(name, slug, req.logoUrl() != null ? req.logoUrl() : brand.getLogoUrl(), req.description() != null ? req.description() : brand.getDescription(), req.website() != null ? req.website() : brand.getWebsite(), req.active() != null ? req.active() : brand.isActive(), req.sortOrder() != null ? req.sortOrder() : brand.getSortOrder());
        return ResponseEntity.ok(BrandResponse.from(repo.save(brand)));
    }

    @DeleteMapping("/admin/brands/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record BrandResponse(Long id, String name, String slug, String logoUrl, String description, String website, boolean active, int sortOrder) {
        public static BrandResponse from(Brand b) {
            return new BrandResponse(b.getId(), b.getName(), b.getSlug(), b.getLogoUrl(), b.getDescription(), b.getWebsite(), b.isActive(), b.getSortOrder());
        }
    }

    public record BrandRequest(String name, String slug, String logoUrl, String description, String website, Boolean active, Integer sortOrder) {}
}
