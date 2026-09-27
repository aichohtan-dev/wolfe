package com.wolfe.catalog;

import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductRelatedController {
    private final ProductRepository repo;
    public ProductRelatedController(ProductRepository repo) {
        this.repo = repo;
    }
    @GetMapping("/{slug}/related") public List<Product> related(@PathVariable String slug, @RequestParam(defaultValue = "4") int limit) {
        Product p = repo.findBySlug(slug).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
        int size = Math.max(1, Math.min(limit, 8));
        return repo.findRelated(p.getId(), p.getCategory(), p.getMaterial(), p.getStyle(), PageRequest.of(0, size));
    }
}
