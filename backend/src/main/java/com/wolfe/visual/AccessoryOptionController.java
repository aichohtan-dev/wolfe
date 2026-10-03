package com.wolfe.visual;

import com.wolfe.catalog.ProductRepository;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class AccessoryOptionController {
    public record AccessoryView(Long id, Long productId, String name, String type, String overlayUrl, String sku, java.math.BigDecimal price, double x, double y, double scale, boolean active) {
        AccessoryView(AccessoryOption a) { this(a.getId(), a.getProductId(), a.getName(), a.getType(), a.getOverlayUrl(), a.getSku(), a.getPrice(), a.getX(), a.getY(), a.getScale(), a.isActive()); }
    }
    private final AccessoryOptionRepository repo;
    private final ProductRepository products;
    public AccessoryOptionController(AccessoryOptionRepository repo, ProductRepository products) {
        this.repo = repo;
        this.products = products;
    }
    @GetMapping("/{slug}/accessories") public List<AccessoryView> list(@PathVariable String slug) {
        var p = products.findBySlug(slug).filter(x -> x.isActive()).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return repo.findByProductIdAndActiveTrueOrderByTypeAscNameAsc(p.getId()).stream().map(AccessoryView::new).toList();
    }
}
