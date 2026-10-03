package com.wolfe.bundle;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bundles")
public class BundleController {
    private final BundleRepository bundles;
    private final BundleItemRepository items;
    private final ProductRepository products;
    public BundleController(BundleRepository bundles, BundleItemRepository items, ProductRepository products) {
        this.bundles = bundles;
        this.items = items;
        this.products = products;
    }
    @GetMapping public List<Map<String, Object>> list() {
        return bundles.findAll(org.springframework.data.domain.PageRequest.of(0, 100, org.springframework.data.domain.Sort.by("id").ascending())).stream().filter(Bundle::isActive).map(this::view).filter(Objects::nonNull).toList();
    }
    @GetMapping("/{slug}") public Map<String, Object> get(@PathVariable String slug) {
        Map<String, Object> result = view(bundles.findBySlug(slug).filter(Bundle::isActive).orElseThrow(() -> new NoSuchElementException("Bundle not found")));
        if (result == null) throw new NoSuchElementException("Bundle not found");
        return result;
    }
    private Map<String, Object> view(Bundle b) {
        var lines = items.findByBundleId(b.getId()).stream().map(i -> products.findById(i.getProductId()).filter(Product::isActive).map(p -> Map.<String,
        Object>of("productId", p.getId(), "slug", p.getSlug(), "name", p.getName(), "price", p.getPrice(), "quantity", i.getQuantity(), "imageUrl",
        p.getImageUrl() == null ? "" : p.getImageUrl())).orElse(null)).filter(Objects::nonNull).toList();
        if (lines.isEmpty()) return null;
        BigDecimal subtotal = lines.stream().map(x -> ((BigDecimal)x.get("price")).multiply(BigDecimal.valueOf((Integer)x.get("quantity")))).reduce(BigDecimal.ZERO,
        BigDecimal::add);
        BigDecimal discount = b.getDiscountType().equals("FIXED")?b.getDiscountValue():subtotal.multiply(b.getDiscountValue()).divide(BigDecimal.valueOf(100),
        2,
        RoundingMode.HALF_UP);
        if (discount.compareTo(subtotal)>0) discount = subtotal;
        BigDecimal total = subtotal.subtract(discount);
        return Map.of("id", b.getId(), "slug", b.getSlug(), "name", b.getName(), "description", b.getDescription() == null?"":b.getDescription(), "discountType",
        b.getDiscountType(), "discountValue", b.getDiscountValue(), "items", lines, "subtotal", subtotal, "discount", discount, "total",
        total);
    }
}
