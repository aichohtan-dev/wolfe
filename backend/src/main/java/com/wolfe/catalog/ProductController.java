package com.wolfe.catalog;

import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductRepository repository;
    private final ProductVariantRepository variants;
    public ProductController(ProductRepository repository, ProductVariantRepository variants) {
        this.repository = repository;
        this.variants = variants;
    }
    @GetMapping public List<Product> list(@RequestParam(required = false) String category, @RequestParam(required = false) String q,
    @RequestParam(required = false) String finish, @RequestParam(required = false) String material, @RequestParam(required = false) String color,
    @RequestParam(required = false) String style, @RequestParam(required = false) Boolean featured,
    @RequestParam(required = false) String sort) {
        List<Product> rows = repository.searchActive(blankToNull(q), blankToNull(category), blankToNull(finish), blankToNull(material), blankToNull(color),
        blankToNull(style));
        if (featured != null && featured) rows = rows.stream().filter(Product::isFeatured).toList();
        Comparator<Product> cmp = switch (sort == null?"featured":sort) {
            case "price_asc" -> Comparator.comparing(Product::getPrice);
            case "price_desc" -> Comparator.comparing(Product::getPrice).reversed();
            case "name_asc" -> Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Product::isFeatured).reversed().thenComparing(Product::getSortOrder).thenComparing(Product::getName,
            String.CASE_INSENSITIVE_ORDER);
        }
        ;
        return rows.stream().sorted(cmp).toList();
    }
    @GetMapping("/suggestions") public List<ProductSuggestion> suggestions(@RequestParam String q) {
        String term = blankToNull(q);
        if (term == null || term.length()<2)return List.of();
        return repository.suggestActive(term, org.springframework.data.domain.PageRequest.of(0, 8)).stream().map(ProductSuggestion::new).toList();
    }
    public record ProductSuggestion(Long id, String slug, String name, String category, String imageUrl, java.math.BigDecimal price) {
        ProductSuggestion(Product p) {
            this(p.getId(), p.getSlug(), p.getName(), p.getCategory(), p.getImageUrl(), p.getPrice());
        }
    }
    @GetMapping("/filters") public Map<String, List<String>> filters() {
        return Map.of("categories", repository.findDistinctCategories(), "materials", repository.findDistinctMaterials(), "colors",
        repository.findDistinctColors(), "styles", repository.findDistinctStyles(), "finishes",
        repository.findDistinctFinishes());
    }
    @GetMapping("/{slug}") public Product get(@PathVariable String slug) {
        return repository.findBySlug(slug).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }
    @GetMapping("/{slug}/variants") public List<ProductVariantView> variants(@PathVariable String slug) {
        Product p = get(slug);
        return variants.findByProductIdAndActiveTrueOrderByOptionNameAscOptionValueAsc(p.getId()).stream().map(ProductVariantView::new).toList();
    }
    public record ProductVariantView(Long id, String optionName, String optionValue, String sku, java.math.BigDecimal priceOverride, boolean active) {
        ProductVariantView(ProductVariant v) {
            this(v.getId(), v.getOptionName(), v.getOptionValue(), v.getSku(), v.getPriceOverride(), v.isActive());
        }
    }
    private String blankToNull(String v) {
        return v == null || v.isBlank()?null:v.trim();
    }
}
