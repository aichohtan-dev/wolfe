package com.wolfe.visual;

import com.wolfe.catalog.ProductRepository;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class AccessoryOptionController {
    private final AccessoryOptionRepository repo;
    private final ProductRepository products;
    public AccessoryOptionController(AccessoryOptionRepository repo, ProductRepository products) {
        this.repo = repo;
        this.products = products;
    }
    @GetMapping("/{slug}/accessories") public List<AccessoryOption> list(@PathVariable String slug) {
        var p = products.findBySlug(slug).filter(x -> x.isActive()).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return repo.findByProductIdAndActiveTrueOrderByTypeAscNameAsc(p.getId());
    }
}
