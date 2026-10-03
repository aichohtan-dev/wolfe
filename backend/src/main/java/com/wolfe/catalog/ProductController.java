package com.wolfe.catalog;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductRepository repository;
    private final ProductVariantRepository variants;
    private final AtomicReference<FilterCache> filterCache = new AtomicReference<>();

    public ProductController(ProductRepository repository, ProductVariantRepository variants) {
        this.repository = repository;
        this.variants = variants;
    }

    @GetMapping
    public List<ProductPublicView> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String finish,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) {
        validateSearchRange(q, minPrice, maxPrice);
        int p = page == null || page < 0 ? 0 : page;
        int s = pageSize == null || pageSize < 1 ? 100 : Math.min(pageSize, 100);
        return queryPaged(category, subcategory, brand, brandId, q, finish, material, color, style, minPrice, maxPrice, featured, sort, p, s).getContent().stream().map(ProductPublicView::new).toList();
    }

    @GetMapping("/paged")
    public PagedProductResponse pagedList(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String finish,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String style,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int pageSize
    ) {
        validateSearchRange(q, minPrice, maxPrice);
        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, pageSize), 100);
        Page<Product> res = queryPaged(category, subcategory, brand, brandId, q, finish, material, color, style, minPrice, maxPrice, featured, sort, p, s);
        return new PagedProductResponse(
                res.getContent().stream().map(ProductPublicView::new).toList(),
                res.getNumber(),
                res.getSize(),
                res.getTotalElements(),
                res.getTotalPages(),
                res.hasNext(),
                res.isFirst(),
                res.isLast()
        );
    }

    private Page<Product> queryPaged(String category, String subcategory, String brand, Long brandId, String q,
                                     String finish, String material, String color, String style,
                                     BigDecimal minPrice, BigDecimal maxPrice, Boolean featured,
                                     String sort, int page, int pageSize) {
        Sort sortObj = buildSort(sort);
        Pageable pageable = PageRequest.of(page, pageSize, sortObj);

        org.springframework.data.jpa.domain.Specification<Product> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));

            if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (subcategory != null && !subcategory.isBlank() && !subcategory.equalsIgnoreCase("All")) {
                predicates.add(cb.equal(cb.lower(root.get("subcategory")), subcategory.trim().toLowerCase()));
            }
            if (brandId != null) {
                predicates.add(cb.equal(root.get("brandId"), brandId));
            }
            if (brand != null && !brand.isBlank() && !brand.equalsIgnoreCase("All")) {
                predicates.add(cb.equal(cb.lower(root.get("brandName")), brand.trim().toLowerCase()));
            }
            if (finish != null && !finish.isBlank() && !finish.equalsIgnoreCase("All")) {
                predicates.add(cb.equal(cb.lower(root.get("finish")), finish.trim().toLowerCase()));
            }
            if (material != null && !material.isBlank() && !material.equalsIgnoreCase("All")) {
                predicates.add(cb.equal(cb.lower(root.get("material")), material.trim().toLowerCase()));
            }
            if (color != null && !color.isBlank() && !color.equalsIgnoreCase("All")) {
                predicates.add(cb.equal(cb.lower(root.get("color")), color.trim().toLowerCase()));
            }
            if (style != null && !style.isBlank() && !style.equalsIgnoreCase("All")) {
                predicates.add(cb.equal(cb.lower(root.get("style")), style.trim().toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (featured != null) {
                predicates.add(cb.equal(root.get("featured"), featured));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + escapeLike(q.trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern, '\\'),
                        cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern, '\\'),
                        cb.like(cb.lower(root.get("slug")), pattern, '\\'),
                        cb.like(cb.lower(cb.coalesce(root.get("brandName"), "")), pattern, '\\'),
                        cb.like(cb.lower(cb.coalesce(root.get("subcategory"), "")), pattern, '\\')
                ));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }

    private Sort buildSort(String sort) {
        String s = sort == null ? "featured" : sort.trim().toLowerCase();
        return switch (s) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.ASC, "id"));
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.ASC, "id"));
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id"));
            case "newest" -> Sort.by(Sort.Direction.DESC, "id");
            default -> Sort.by(Sort.Direction.DESC, "featured")
                    .and(Sort.by(Sort.Direction.ASC, "sortOrder"))
                    .and(Sort.by(Sort.Direction.ASC, "name"));
        };
    }

    @GetMapping("/suggestions")
    public List<ProductSuggestion> suggestions(@RequestParam String q) {
        String term = blankToNull(q);
        if (term == null || term.length() < 2) return List.of();
        return repository.suggestActive(term, PageRequest.of(0, 8)).stream().map(ProductSuggestion::new).toList();
    }

    public record ProductSuggestion(Long id, String slug, String name, String category, String subcategory, String brandName, String imageUrl, BigDecimal price) {
        ProductSuggestion(Product p) {
            this(p.getId(), p.getSlug(), p.getName(), p.getCategory(), p.getSubcategory(), p.getBrandName(), p.getImageUrl(), p.getPrice());
        }
    }

    @GetMapping("/filters")
    public Map<String, Object> filters() {
        FilterCache cached = filterCache.get();
        if (cached != null && cached.expiresAt > System.currentTimeMillis()) return cached.value;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("categories", repository.findDistinctCategories());
        map.put("subcategories", repository.findDistinctSubcategories());
        map.put("brands", repository.findDistinctBrands());
        map.put("materials", repository.findDistinctMaterials());
        map.put("colors", repository.findDistinctColors());
        map.put("styles", repository.findDistinctStyles());
        map.put("finishes", repository.findDistinctFinishes());
        map.put("minPrice", repository.findMinPrice());
        map.put("maxPrice", repository.findMaxPrice());
        Map<String, Object> immutable = Map.copyOf(map);
        filterCache.set(new FilterCache(immutable, System.currentTimeMillis() + 60_000));
        return immutable;
    }

    private record FilterCache(Map<String, Object> value, long expiresAt) {}

    @GetMapping("/{slug}")
    public ProductPublicView get(@PathVariable String slug) {
        Product p = repository.findBySlugIgnoreCase(slug.trim()).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return new ProductPublicView(p);
    }

    @GetMapping("/{slug}/variants")
    public List<ProductVariantFullView> variants(@PathVariable String slug) {
        Product p = repository.findBySlugIgnoreCase(slug.trim()).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return variants.findByProductIdAndActiveTrueOrderBySortOrderAscIdAsc(p.getId()).stream().map(ProductVariantFullView::new).toList();
    }

    public record ProductVariantFullView(
            Long id,
            Long productId,
            String optionName,
            String optionValue,
            String title,
            String sku,
            String color,
            String material,
            String size,
            String finish,
            String dimensions,
            BigDecimal price,
            BigDecimal priceOverride,
            String imageUrl,
            String attributesJson,
            boolean active
    ) {
        public ProductVariantFullView(ProductVariant v) {
            this(
                    v.getId(),
                    v.getProduct().getId(),
                    v.getOptionName(),
                    v.getOptionValue(),
                    v.getTitle(),
                    v.getSku(),
                    v.getColor(),
                    v.getMaterial(),
                    v.getSize(),
                    v.getFinish(),
                    v.getDimensions(),
                    v.getPrice(),
                    v.getPriceOverride(),
                    v.getImageUrl(),
                    v.getAttributesJson(),
                    v.isActive()
            );
        }
    }

    public record ProductPublicView(Long id, String slug, String name, BigDecimal price, String category, String finish,
                                    String material, String color, String style, String description, String imageUrl, String mediaUrls,
                                    Long brandId, String brandName, String subcategory, String dimensions, String modelNumber,
                                    boolean active, boolean featured, int sortOrder) {
        public ProductPublicView(Product p) {
            this(p.getId(), p.getSlug(), p.getName(), p.getPrice(), p.getCategory(), p.getFinish(), p.getMaterial(), p.getColor(),
                    p.getStyle(), p.getDescription(), p.getImageUrl(), p.getMediaUrls(), p.getBrandId(), p.getBrandName(),
                    p.getSubcategory(), p.getDimensions(), p.getModelNumber(), p.isActive(), p.isFeatured(), p.getSortOrder());
        }
    }

    public record PagedProductResponse(
            List<ProductPublicView> content,
            int page,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean isFirst,
            boolean isLast
    ) {}

    private static void validateSearchRange(String q, BigDecimal minPrice, BigDecimal maxPrice) {
        if (q != null && q.length() > 120) throw new IllegalArgumentException("search query is too long");
        if (minPrice != null && minPrice.signum() < 0) throw new IllegalArgumentException("minPrice cannot be negative");
        if (maxPrice != null && maxPrice.signum() < 0) throw new IllegalArgumentException("maxPrice cannot be negative");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) throw new IllegalArgumentException("minPrice cannot exceed maxPrice");
    }
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
