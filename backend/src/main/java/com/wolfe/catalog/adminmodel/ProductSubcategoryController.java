package com.wolfe.catalog.adminmodel;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProductSubcategoryController {
    private final ProductSubcategoryRepository repo;

    public ProductSubcategoryController(ProductSubcategoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/subcategories")
    public List<ProductSubcategory> listActive(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return repo.findByCategoryNameAndActiveTrueOrderBySortOrderAscNameAsc(category.trim());
        }
        return repo.findByActiveTrueOrderByCategoryNameAscSortOrderAscNameAsc();
    }

    @GetMapping("/admin/subcategories")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<ProductSubcategory> listAdmin() {
        return repo.findAllByOrderByCategoryNameAscSortOrderAscNameAsc();
    }

    @PostMapping("/admin/subcategories")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ProductSubcategory> create(@RequestBody SubcategoryRequest req) {
        if (req == null || req.name() == null || req.name().isBlank() || req.categoryName() == null || req.categoryName().isBlank()) {
            throw new IllegalArgumentException("Subcategory name and category name are required");
        }
        String name = req.name().trim();
        String cat = req.categoryName().trim();
        String slug = req.slug() != null && !req.slug().isBlank() ? req.slug().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-") : name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        ProductSubcategory sub = new ProductSubcategory(req.categoryId(), cat, name, slug, req.description(), req.active() == null || req.active(), req.sortOrder() == null ? 0 : req.sortOrder());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(sub));
    }

    @PutMapping("/admin/subcategories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ProductSubcategory> update(@PathVariable Long id, @RequestBody SubcategoryRequest req) {
        ProductSubcategory sub = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Subcategory not found"));
        String name = req.name() != null && !req.name().isBlank() ? req.name().trim() : sub.getName();
        String cat = req.categoryName() != null && !req.categoryName().isBlank() ? req.categoryName().trim() : sub.getCategoryName();
        String slug = req.slug() != null && !req.slug().isBlank() ? req.slug().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-") : sub.getSlug();
        sub.update(cat, name, slug, req.description() != null ? req.description() : sub.getDescription(), req.active() != null ? req.active() : sub.isActive(), req.sortOrder() != null ? req.sortOrder() : sub.getSortOrder());
        return ResponseEntity.ok(repo.save(sub));
    }

    @DeleteMapping("/admin/subcategories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public record SubcategoryRequest(Long categoryId, String categoryName, String name, String slug, String description, Boolean active, Integer sortOrder) {}
}
