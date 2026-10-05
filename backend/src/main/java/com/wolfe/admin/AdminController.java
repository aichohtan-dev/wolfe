package com.wolfe.admin;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.catalog.adminmodel.*;
import com.wolfe.customer.Customer;
import com.wolfe.customer.CustomerRepository;
import com.wolfe.inventory.Inventory;
import com.wolfe.inventory.InventoryRepository;
import com.wolfe.order.Order;
import com.wolfe.order.OrderRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final com.wolfe.review.ProductReviewRepository reviews;
    private final com.wolfe.quote.QuoteRequestRepository quotes;
    private final com.wolfe.customdesign.CustomDesignRequestRepository customDesigns;
    private final com.wolfe.discount.CouponRepository coupons;
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ProductCategoryRepository categoriesRepo;
    private final ProductCollectionRepository collectionsRepo;
    private final ProductMediaRepository mediaRepo;
    private final InventoryRepository inventory;
    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final com.wolfe.returning.ReturnRequestRepository returnRequests;
    private final com.wolfe.notification.CustomerNotificationRepository notifications;
    private final com.wolfe.order.OrderStatusHistoryRepository history;
    private final com.wolfe.notification.NotificationService notificationService;
    private final com.wolfe.order.OrderService orderService;
    private final com.wolfe.visual.VisualContentRepository visualContents;
    private final com.wolfe.visual.AccessoryOptionRepository accessories;
    private final com.wolfe.experience.StockSubscriptionRepository stockSubscriptions;
    private final com.wolfe.bundle.BundleRepository bundles;
    private final com.wolfe.bundle.BundleItemRepository bundleItems;
    private final com.wolfe.returning.ReturnProcessingService returnProcessingService;
    private final com.wolfe.cart.CartItemRepository cartItems;
    private final com.wolfe.wishlist.WishlistItemRepository wishlistItems;
    public AdminController(com.wolfe.review.ProductReviewRepository reviews, com.wolfe.quote.QuoteRequestRepository quotes,
    com.wolfe.customdesign.CustomDesignRequestRepository customDesigns, ProductRepository products, com.wolfe.discount.CouponRepository coupons,
    ProductVariantRepository variants, ProductCategoryRepository categoriesRepo, ProductCollectionRepository collectionsRepo,
    ProductMediaRepository mediaRepo, InventoryRepository inventory, OrderRepository orders, CustomerRepository customers,
    com.wolfe.returning.ReturnRequestRepository returnRequests, com.wolfe.notification.CustomerNotificationRepository notifications,
    com.wolfe.order.OrderStatusHistoryRepository history, com.wolfe.notification.NotificationService notificationService,
    com.wolfe.order.OrderService orderService, com.wolfe.visual.VisualContentRepository visualContents,
    com.wolfe.visual.AccessoryOptionRepository accessories, com.wolfe.experience.StockSubscriptionRepository stockSubscriptions,
    com.wolfe.bundle.BundleRepository bundles,
    com.wolfe.bundle.BundleItemRepository bundleItems, com.wolfe.returning.ReturnProcessingService returnProcessingService,
    com.wolfe.cart.CartItemRepository cartItems, com.wolfe.wishlist.WishlistItemRepository wishlistItems) {
        this.reviews = reviews;
        this.quotes = quotes;
        this.customDesigns = customDesigns;
        this.products = products;
        this.coupons = coupons;
        this.variants = variants;
        this.categoriesRepo = categoriesRepo;
        this.collectionsRepo = collectionsRepo;
        this.mediaRepo = mediaRepo;
        this.inventory = inventory;
        this.orders = orders;
        this.customers = customers;
        this.returnRequests = returnRequests;
        this.notifications = notifications;
        this.history = history;
        this.notificationService = notificationService;
        this.orderService = orderService;
        this.visualContents = visualContents;
        this.accessories = accessories;
        this.stockSubscriptions = stockSubscriptions;
        this.bundles = bundles;
        this.bundleItems = bundleItems;
        this.returnProcessingService = returnProcessingService;
        this.cartItems = cartItems;
        this.wishlistItems = wishlistItems;
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_ORDERS')")
    @GetMapping("/dashboard") public Map<String, Object> dashboard() {
        long revenue = orders.sumRevenueExcludingCancelled();
        long orderCount = orders.count();
        long low = inventory.countByAvailableLessThanEqual(5);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("products", products.count());
        map.put("activeProducts", products.countByActiveTrue());
        map.put("orders", orderCount);
        map.put("customers", customers.count());
        map.put("inventoryItems", inventory.count());
        map.put("lowStock", low);
        map.put("reviews", reviews.count());
        map.put("pendingReviews", reviews.countByStatus("PENDING"));
        map.put("quotes", quotes.count());
        map.put("openQuotes", quotes.countByStatusNot("CLOSED"));
        map.put("customDesigns", customDesigns.count());
        map.put("openCustomDesigns", customDesigns.countByStatusNotIn(Set.of("COMPLETED", "CLOSED")));
        map.put("returns", returnRequests.count());
        map.put("revenue", revenue);
        map.put("confirmed", orders.countByStatus("CONFIRMED"));
        map.put("processing", orders.countByStatus("PROCESSING"));
        map.put("shipped", orders.countByStatus("SHIPPED"));
        map.put("delivered", orders.countByStatus("DELIVERED"));
        return map;
    }
    public record CouponRequest(@NotBlank String code, @NotBlank String discountType, @Positive long value, @Min(0) long minimumSubtotal,
    @Min(0) long maximumDiscount, @Min(1) Integer usageLimit, @Min(1) Integer perCustomerUsageLimit, Boolean active, java.time.Instant startsAt,
    java.time.Instant expiresAt) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/coupons") public List<com.wolfe.discount.Coupon> coupons(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return coupons.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by("code"))).getContent();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.CREATED) public com.wolfe.discount.Coupon createCoupon(@Valid @RequestBody CouponRequest r) {
        if (coupons.findByCode(r.code().trim().toUpperCase()).isPresent())throw new IllegalArgumentException("coupon code already exists");
        var c = new com.wolfe.discount.Coupon();
        c.configure(r.code(), r.discountType(), r.value(), r.minimumSubtotal(), r.maximumDiscount(), r.usageLimit(), r.perCustomerUsageLimit(), r.active() == null || r.active(), r.startsAt(),
        r.expiresAt());
        return coupons.save(c);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/coupons/{id}") public com.wolfe.discount.Coupon updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest r) {
        var c = coupons.findById(id).orElseThrow(() -> new NoSuchElementException("Coupon not found"));
        if (coupons.findByCode(r.code().trim().toUpperCase()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("coupon code already exists");
        c.configure(r.code(), r.discountType(), r.value(), r.minimumSubtotal(), r.maximumDiscount(), r.usageLimit(), r.perCustomerUsageLimit(), r.active() == null?c.isActive():r.active(),
        r.startsAt(),
        r.expiresAt());
        return coupons.save(c);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/coupons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteCoupon(@PathVariable Long id) {
        var c = coupons.findById(id).orElseThrow(() -> new NoSuchElementException("Coupon not found"));
        c.configure(c.getCode(), c.getDiscountType(), c.getValue(), c.getMinimumSubtotal(), c.getMaximumDiscount(), c.getUsageLimit(), c.getPerCustomerUsageLimit(), false, c.getStartsAt(),
        c.getExpiresAt());
        coupons.save(c);
    }
    public record BundleRequest(@NotBlank String slug, @NotBlank String name, String description, @NotBlank String discountType,
    @NotNull @PositiveOrZero BigDecimal discountValue, Boolean active,
    @NotEmpty List<Long> productIds) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/bundles") public List<Map<String, Object>> bundles(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        var content = bundles.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by("id"))).getContent();
        var itemMap = bundleItems.findByBundleIdIn(content.stream().map(com.wolfe.bundle.Bundle::getId).toList()).stream().collect(java.util.stream.Collectors.groupingBy(com.wolfe.bundle.BundleItem::getBundleId));
        return content.stream().map(b -> {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", b.getId()); m.put("slug", b.getSlug()); m.put("name", b.getName());
            m.put("description", b.getDescription() == null ? "" : b.getDescription());
            m.put("discountType", b.getDiscountType()); m.put("discountValue", b.getDiscountValue()); m.put("active", b.isActive());
            m.put("productIds", itemMap.getOrDefault(b.getId(), List.of()).stream().map(com.wolfe.bundle.BundleItem::getProductId).toList());
            return m;
        }).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/bundles")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED) public Map<String, Object> createBundle(@Valid @RequestBody BundleRequest r) {
        if (bundles.findBySlug(r.slug().trim()).isPresent())throw new IllegalArgumentException("bundle slug already exists");
        validateBundleProducts(r.productIds());
        validateBundleDiscount(r.discountType(), r.discountValue());
        var b = bundles.save(new com.wolfe.bundle.Bundle(r.slug(), r.name(), r.description(), r.discountType(), r.discountValue(),
        r.active() == null || r.active()));
        for (Long pid:new LinkedHashSet<>(r.productIds()))bundleItems.save(new com.wolfe.bundle.BundleItem(b.getId(), pid, 1));
        return Map.of("id", b.getId(), "slug", b.getSlug());
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/bundles/{id}")
    @Transactional public Map<String, Object> updateBundle(@PathVariable Long id, @Valid @RequestBody BundleRequest r) {
        var b = bundles.findById(id).orElseThrow(() -> new NoSuchElementException("Bundle not found"));
        validateBundleProducts(r.productIds());
        validateBundleDiscount(r.discountType(), r.discountValue());
        if (bundles.findBySlug(r.slug().trim()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("bundle slug already exists");
        b.configure(r.slug(), r.name(), r.description(), r.discountType(), r.discountValue(), r.active() == null?b.isActive():r.active());
        bundles.save(b);
        bundleItems.deleteByBundleId(id);
        for (Long pid:new LinkedHashSet<>(r.productIds()))bundleItems.save(new com.wolfe.bundle.BundleItem(id, pid, 1));
        return Map.of("id", b.getId(), "slug", b.getSlug());
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/bundles/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteBundle(@PathVariable Long id) {
        var b = bundles.findById(id).orElseThrow(() -> new NoSuchElementException("Bundle not found"));
        b.configure(b.getSlug(), b.getName(), b.getDescription(), b.getDiscountType(), b.getDiscountValue(), false);
        bundles.save(b);
    }
    private static void validateMediaUrl(String value) {
        if (value == null || value.isBlank()) return;
        String v = value.trim();
        if (v.startsWith("/catalog/published/") || v.startsWith("data:image/")) return;
        try {
            var u = java.net.URI.create(v);
            if (!Set.of("https").contains(u.getScheme()) || u.getHost() == null) throw new IllegalArgumentException("imageUrl must be an HTTPS URL or internal published path");
        } catch (IllegalArgumentException e) { throw new IllegalArgumentException("invalid imageUrl"); }
    }
    private static void validateMediaUrls(String value) {
        if (value == null || value.isBlank()) return;
        for (String item : value.split("[,\n]")) validateMediaUrl(item);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validateBundleProducts(List<Long> ids) {
        if (ids == null || ids.size()<2 || ids.size()>10)throw new IllegalArgumentException("bundle must contain 2-10 products");
        for (Long id:new LinkedHashSet<>(ids)) {
            var p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found: "+id));
            if (!p.isActive())throw new IllegalArgumentException("bundle product must be active: "+p.getSlug());
        }
    }

    private static void validateBundleDiscount(String discountType, BigDecimal discountValue) {
        String type = discountType == null ? "" : discountType.trim().toUpperCase(java.util.Locale.ROOT);
        if (!Set.of("FIXED", "PERCENT").contains(type)) {
            throw new IllegalArgumentException("bundle discount type must be FIXED or PERCENT");
        }
        if (discountValue == null || discountValue.signum() < 0) {
            throw new IllegalArgumentException("bundle discount value must be non-negative");
        }
        if ("PERCENT".equals(type) && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("bundle percentage discount cannot exceed 100");
        }
    }
    public record ProductAdminView(Long id, String slug, String name, BigDecimal price, String category, String subcategory, Long brandId, String brandName, String finish, String material, String color, String style, String dimensions, String modelNumber, String description, String imageUrl, String mediaUrls, String attributesJson, boolean active, boolean featured, int sortOrder) {
        ProductAdminView(Product p) { this(p.getId(),p.getSlug(),p.getName(),p.getPrice(),p.getCategory(),p.getSubcategory(),p.getBrandId(),p.getBrandName(),p.getFinish(),p.getMaterial(),p.getColor(),p.getStyle(),p.getDimensions(),p.getModelNumber(),p.getDescription(),p.getImageUrl(),p.getMediaUrls(),p.getAttributesJson(),p.isActive(),p.isFeatured(),p.getSortOrder()); }
    }
    public record ProductRequest(@NotBlank @Size(max=120) @jakarta.validation.constraints.Pattern(regexp="^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug,
    @NotBlank @Size(max=200) String name, @Positive BigDecimal price, @NotBlank @Size(max=120) String category,
    @Size(max=120) String subcategory, Long brandId, @Size(max=120) String brandName, @NotBlank @Size(max=120) String finish, @NotBlank @Size(max=100) String material, @NotBlank @Size(max=100) String color,
    @NotBlank @Size(max=100) String style, @Size(max=200) String dimensions, @Size(max=120) String modelNumber, @Size(max=2000) String description, @Size(max=1000) String imageUrl, @Size(max=6000) String mediaUrls, @Size(max=12000) String attributesJson,
    Boolean active, Boolean featured, Integer sortOrder) {
    }
    public record VariantRequest(@NotBlank String optionName, @NotBlank String optionValue, String title,
    @NotBlank @Size(max = 120) String sku, String color, String material, String size, String finish, String dimensions,
    @Positive BigDecimal priceOverride, @Min(0) Integer stockQuantity, String imageUrl, String attributesJson, Integer sortOrder, Boolean active) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/products") public List<ProductAdminView> productList(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return products.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by("sortOrder").ascending().and(org.springframework.data.domain.Sort.by("name").ascending())) ).getContent().stream().map(ProductAdminView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED) public ProductAdminView createProduct(@Valid @RequestBody ProductRequest r) {
        ensureCategory(r.category());
        validateMediaUrl(r.imageUrl());
        validateMediaUrls(r.mediaUrls());
        if (products.findBySlug(r.slug()).isPresent())throw new IllegalArgumentException("slug already exists");
        Product p = new Product(r.slug().trim(), r.name().trim(), r.price(), r.category().trim(), r.finish().trim(), r.description());
        p.setImageUrl(r.imageUrl());
        p.setMediaUrls(r.mediaUrls());
        p.setSubcategory(blankToNull(r.subcategory()));
        p.setBrandId(r.brandId());
        p.setBrandName(blankToNull(r.brandName()));
        p.setDimensions(blankToNull(r.dimensions()));
        p.setModelNumber(blankToNull(r.modelNumber()));
        p.setAttributesJson(blankToNull(r.attributesJson()));
        p.setMaterial(r.material().trim());
        p.setColor(r.color().trim());
        p.setStyle(r.style().trim());
        p.setActive(r.active() == null || r.active());
        p.setFeatured(r.featured() != null && r.featured());
        p.setSortOrder(r.sortOrder() == null?0:r.sortOrder());
        p = products.save(p);
        inventory.insertDefault(p.getId(), 0);
        return new ProductAdminView(p);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/products/{id}") public ProductAdminView updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest r) {
        ensureCategory(r.category());
        validateMediaUrl(r.imageUrl());
        validateMediaUrls(r.mediaUrls());
        Product p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        if (products.findBySlug(r.slug()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("slug already exists");
        p.updateFull(r.name().trim(), r.price(), r.category().trim(), blankToNull(r.subcategory()), r.brandId(),
        blankToNull(r.brandName()), r.finish().trim(), r.material().trim(), r.color().trim(), r.style().trim(),
        blankToNull(r.dimensions()), blankToNull(r.modelNumber()), r.description(), r.imageUrl(), r.mediaUrls(),
        blankToNull(r.attributesJson()), r.active() == null?p.isActive():r.active(),
        r.featured() == null?p.isFeatured():r.featured(), r.sortOrder() == null?p.getSortOrder():r.sortOrder());
        return new ProductAdminView(products.save(p));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/products/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteProduct(@PathVariable Long id) {
        Product p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        p.setActive(false);
        products.save(p);
        cartItems.deleteByProductId(id);
        wishlistItems.deleteByProductId(id);
        for (var bundleItem : bundleItems.findByProductId(id)) {
            var bundle = bundles.findById(bundleItem.getBundleId()).orElse(null);
            if (bundle != null && bundle.isActive() && bundleItems.findByBundleId(bundle.getId()).size() <= 2) {
                bundle.configure(bundle.getSlug(), bundle.getName(), bundle.getDescription(), bundle.getDiscountType(), bundle.getDiscountValue(), false);
                bundles.save(bundle);
            }
        }
        bundleItems.deleteByProductId(id);
    }
    public record BulkProductRequest(@NotEmpty List<Long> ids, Boolean active, Boolean featured, Integer sortOrderDelta) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/products/bulk")
    @Transactional public List<ProductAdminView> bulkProducts(@Valid @RequestBody BulkProductRequest r) {
        var result = new ArrayList<Product>();
        for (Long id:r.ids()) {
            Product p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found: "+id));
            if (r.active() != null)p.setActive(r.active());
            if (r.featured() != null)p.setFeatured(r.featured());
            if (r.sortOrderDelta() != null)p.setSortOrder(Math.max(0, p.getSortOrder()+r.sortOrderDelta()));
            result.add(products.save(p));
        }
        return result.stream().map(ProductAdminView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/products/{id}/variants") public List<ProductVariantView> variants(@PathVariable Long id, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return variants.findByProductIdAndActiveTrueOrderByOptionNameAscOptionValueAsc(id, org.springframework.data.domain.PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100))).getContent().stream().map(ProductVariantView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/products/{id}/variants")
    @ResponseStatus(HttpStatus.CREATED) public ProductVariantView createVariant(@PathVariable Long id, @Valid @RequestBody VariantRequest r) {
        var p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        var v = new ProductVariant(p, r.title() == null || r.title().isBlank() ? r.optionValue().trim() : r.title().trim(), r.sku().trim().toUpperCase(),
                r.color(), r.material(), r.size(), r.finish(), r.dimensions(), r.priceOverride(), r.stockQuantity() == null ? 0 : r.stockQuantity(),
                r.imageUrl(), r.attributesJson(), r.active() == null || r.active(), r.sortOrder() == null ? 0 : r.sortOrder());
        v.setSku(r.sku().trim().toUpperCase());
        v.setTitle(r.title() == null || r.title().isBlank() ? r.optionValue().trim() : r.title().trim());
        v.setColor(r.color()); v.setMaterial(r.material()); v.setSize(r.size()); v.setFinish(r.finish()); v.setDimensions(r.dimensions());
        v.setAttributesJson(r.attributesJson()); v.setImageUrl(r.imageUrl()); v.setSortOrder(r.sortOrder() == null ? 0 : r.sortOrder());
        v.setActive(r.active() == null || r.active());
        v = variants.save(v);
        return new ProductVariantView(v);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/products/{id}/variants/{variantId}") public ProductVariantView updateVariant(@PathVariable Long id, @PathVariable Long variantId,
    @Valid @RequestBody VariantRequest r) {
        var v = variants.findById(variantId).orElseThrow(() -> new NoSuchElementException("Variant not found"));
        if (!v.getProduct().getId().equals(id))throw new IllegalArgumentException("Variant does not belong to product");
        v.updateFull(r.title() == null || r.title().isBlank() ? r.optionValue().trim() : r.title().trim(), r.sku().trim().toUpperCase(),
                r.color(), r.material(), r.size(), r.finish(), r.dimensions(), r.priceOverride(), r.stockQuantity() == null ? v.getStockQuantity() : r.stockQuantity(),
                r.imageUrl(), r.attributesJson(), r.active() == null || r.active(), r.sortOrder() == null ? v.getSortOrder() : r.sortOrder());
        v.setSku(r.sku().trim().toUpperCase()); v.setColor(r.color()); v.setMaterial(r.material()); v.setSize(r.size()); v.setFinish(r.finish()); v.setDimensions(r.dimensions());
        return new ProductVariantView(variants.save(v));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/products/{id}/variants/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteVariant(@PathVariable Long id, @PathVariable Long variantId) {
        var v = variants.findById(variantId).orElseThrow(() -> new NoSuchElementException("Variant not found"));
        if (!v.getProduct().getId().equals(id))throw new IllegalArgumentException("Variant does not belong to product");
        v.update(v.getOptionName(), v.getOptionValue(), v.getSku(), v.getPriceOverride(), false);
        variants.save(v);
    }
    public record ProductVariantView(Long id, Long productId, String optionName, String optionValue, String title, String sku,
    String color, String material, String size, String finish, String dimensions, BigDecimal price, BigDecimal priceOverride,
    int stockQuantity, String imageUrl, String attributesJson, int sortOrder, boolean active) {
        ProductVariantView(ProductVariant v) {
            this(v.getId(), v.getProduct().getId(), v.getOptionName(), v.getOptionValue(), v.getTitle(), v.getSku(),
            v.getColor(), v.getMaterial(), v.getSize(), v.getFinish(), v.getDimensions(), v.getPrice(), v.getPriceOverride(),
            v.getStockQuantity(), v.getImageUrl(), v.getAttributesJson(), v.getSortOrder(), v.isActive());
        }
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/categories") public List<ProductCategory> categories(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return categoriesRepo.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by("name"))).getContent();
    }
    public record CategoryRequest(@NotBlank String name, Boolean active) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED) public ProductCategory createCategory(@Valid @RequestBody CategoryRequest r) {
        if (categoriesRepo.findByNameIgnoreCase(r.name().trim()).isPresent())throw new IllegalArgumentException("category already exists");
        return categoriesRepo.save(new ProductCategory(r.name().trim()));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/categories/{id}") public ProductCategory updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest r) {
        var c = categoriesRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found"));
        if (categoriesRepo.findByNameIgnoreCase(r.name().trim()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("category already exists");
        c.update(r.name().trim(), r.active() == null?c.isActive():r.active());
        return categoriesRepo.save(c);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteCategory(@PathVariable Long id) {
        var c = categoriesRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found"));
        c.update(c.getName(), false);
        categoriesRepo.save(c);
    }
    private void ensureCategory(String name) {
        if (categoriesRepo.findByNameIgnoreCase(name.trim()).isEmpty())categoriesRepo.save(new ProductCategory(name.trim()));
    }
    public record CollectionRequest(@NotBlank String name, String description, Boolean active, List<Long> productIds) {
    }
    public record CollectionView(Long id, String name, String description, boolean active, List<Long> productIds, int productCount) {
        CollectionView(ProductCollection c) {
            this(c.getId(), c.getName(), c.getDescription(), c.isActive(), c.getProducts().stream().map(Product::getId).toList(), c.getProducts().size());
        }
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/collections")
    @Transactional(readOnly = true) public List<CollectionView> collections(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return collectionsRepo.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by("name"))).getContent().stream().map(CollectionView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/collections")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional public CollectionView createCollection(@Valid @RequestBody CollectionRequest r) {
        if (collectionsRepo.findByNameIgnoreCase(r.name().trim()).isPresent())throw new IllegalArgumentException("collection already exists");
        var c = new ProductCollection(r.name().trim(), r.description());
        c.update(r.name().trim(), r.description(), r.active() == null || r.active());
        applyProducts(c, r.productIds());
        return new CollectionView(collectionsRepo.save(c));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/collections/{id}")
    @Transactional public CollectionView updateCollection(@PathVariable Long id, @Valid @RequestBody CollectionRequest r) {
        var c = collectionsRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Collection not found"));
        if (collectionsRepo.findByNameIgnoreCase(r.name().trim()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("collection already exists");
        c.update(r.name().trim(), r.description(), r.active() == null?c.isActive():r.active());
        applyProducts(c, r.productIds());
        return new CollectionView(collectionsRepo.save(c));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/collections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteCollection(@PathVariable Long id) {
        collectionsRepo.deleteById(id);
    }
    private void applyProducts(ProductCollection c, List<Long> ids) {
        c.getProducts().clear();
        if (ids != null)ids.stream().distinct().map(x -> products.findById(x).orElseThrow(() -> new NoSuchElementException("Product not found: "+x))).forEach(c.getProducts()::add);
    }
    public record MediaRequest(@NotBlank String type, @NotBlank String url, String altText, @Min(0) Integer sortOrder, Boolean active) {
    }
    public record MediaView(Long id, Long productId, String type, String url, String altText, int sortOrder, boolean active) {
        MediaView(ProductMedia m) {
            this(m.getId(), m.getProduct().getId(), m.getType(), m.getUrl(), m.getAltText(), m.getSortOrder(), m.isActive());
        }
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/products/{id}/media") public List<MediaView> media(@PathVariable Long id, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return mediaRepo.findByProductIdOrderBySortOrderAscIdAsc(id, org.springframework.data.domain.PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100))).getContent().stream().map(MediaView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/products/{id}/media")
    @ResponseStatus(HttpStatus.CREATED) public MediaView createMedia(@PathVariable Long id, @Valid @RequestBody MediaRequest r) {
        var p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        String type = r.type().trim().toUpperCase();
        if (!Set.of("IMAGE", "VIDEO", "360").contains(type))throw new IllegalArgumentException("media type must be IMAGE, VIDEO or 360");
        return new MediaView(mediaRepo.save(new ProductMedia(p, type, r.url().trim(), r.altText(), r.sortOrder() == null?0:r.sortOrder(),
        r.active() == null || r.active())));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/products/{id}/media/{mediaId}") public MediaView updateMedia(@PathVariable Long id, @PathVariable Long mediaId,
    @Valid @RequestBody MediaRequest r) {
        var m = mediaRepo.findById(mediaId).orElseThrow(() -> new NoSuchElementException("Media not found"));
        if (!m.getProduct().getId().equals(id))throw new IllegalArgumentException("Media does not belong to product");
        String type = r.type().trim().toUpperCase();
        if (!Set.of("IMAGE", "VIDEO", "360").contains(type))throw new IllegalArgumentException("media type must be IMAGE, VIDEO or 360");
        m.update(type, r.url().trim(), r.altText(), r.sortOrder() == null?m.getSortOrder():r.sortOrder(), r.active() == null?m.isActive():r.active());
        return new MediaView(mediaRepo.save(m));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/products/{id}/media/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteMedia(@PathVariable Long id, @PathVariable Long mediaId) {
        var m = mediaRepo.findById(mediaId).orElseThrow(() -> new NoSuchElementException("Media not found"));
        if (!m.getProduct().getId().equals(id))throw new IllegalArgumentException("Media does not belong to product");
        m.deactivate();
        mediaRepo.save(m);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/inventory") public List<InventoryView> inventory(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return inventory.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by("productId"))).getContent().stream().map(i -> new InventoryView(i.getProduct().getId(), i.getProduct().getSlug(), i.getProduct().getName(),
        i.getQuantity(), i.getReserved(),
        i.getAvailable())).toList();
    }
    public record InventoryView(Long productId, String slug, String name, int quantity, int reserved, int available) {
    }
    public record StockRequest(@Min(0) int quantity) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @org.springframework.transaction.annotation.Transactional
    @PutMapping("/inventory/{productId}") public InventoryView updateStock(@PathVariable Long productId, @Valid @RequestBody StockRequest r) {
        var p = products.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        if (!variants.findByProductIdAndActiveTrueOrderByOptionNameAscOptionValueAsc(productId, org.springframework.data.domain.PageRequest.of(0, 1)).getContent().isEmpty()) {
            throw new IllegalStateException("Use variant stock management for products with active variants");
        }
        var i = inventory.findByProductIdForUpdate(productId).orElseGet(() -> {
            inventory.insertDefault(p.getId(), 0);
            return inventory.findByProductId(productId).orElseThrow(() -> new NoSuchElementException("Inventory not found"));
        });
        int before = i.getAvailable();
        i.setQuantity(r.quantity());
        i = inventory.save(i);
        if (before <= 0 && i.getAvailable()>0) {
            for (var sub:stockSubscriptions.findByProductIdAndActiveTrue(productId)) {
                notificationService.stockAlert(sub.getCustomerId(), p.getSlug(), p.getName(), i.getAvailable());
                sub.setActive(false);
                stockSubscriptions.save(sub);
            }
        }
        return new InventoryView(p.getId(), p.getSlug(), p.getName(), i.getQuantity(), i.getReserved(), i.getAvailable());
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/stock-alerts") public List<InventoryView> stockAlerts(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return inventory.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by("productId"))).getContent().stream().filter(i -> i.getAvailable() <= 5).map(i -> new InventoryView(i.getProduct().getId(), i.getProduct().getSlug(),
        i.getProduct().getName(), i.getQuantity(), i.getReserved(),
        i.getAvailable())).toList();
    }
    public record VisualContentRequest(@NotBlank String placement, @NotBlank String title, String subtitle, @NotBlank String mediaType,
    @NotBlank String mediaUrl, String posterUrl, String linkUrl, Boolean active,
    Integer sortOrder) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/visual-content") public List<com.wolfe.visual.VisualContent> visualContent(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return visualContents.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by("placement").ascending().and(org.springframework.data.domain.Sort.by("sortOrder").ascending()).and(org.springframework.data.domain.Sort.by("id").ascending()))).getContent();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/visual-content")
    @ResponseStatus(HttpStatus.CREATED) public com.wolfe.visual.VisualContent createVisualContent(@Valid @RequestBody VisualContentRequest r) {
        return visualContents.save(new com.wolfe.visual.VisualContent(r.placement().trim().toUpperCase(), r.title().trim(), r.subtitle(),
        r.mediaType().trim().toUpperCase(), r.mediaUrl().trim(), r.posterUrl(), r.linkUrl(), r.active() == null || r.active(),
        r.sortOrder() == null?0:r.sortOrder()));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/visual-content/{id}") public com.wolfe.visual.VisualContent updateVisualContent(@PathVariable Long id,
    @Valid @RequestBody VisualContentRequest r) {
        var x = visualContents.findById(id).orElseThrow(() -> new NoSuchElementException("Visual content not found"));
        x.update(r.placement().trim().toUpperCase(), r.title().trim(), r.subtitle(), r.mediaType().trim().toUpperCase(), r.mediaUrl().trim(), r.posterUrl(),
        r.linkUrl(), r.active() == null?x.isActive():r.active(),
        r.sortOrder() == null?x.getSortOrder():r.sortOrder());
        return visualContents.save(x);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/visual-content/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteVisualContent(@PathVariable Long id) {
        visualContents.deleteById(id);
    }
    public record AccessoryRequest(@NotBlank String name, @NotBlank String type, @NotBlank String overlayUrl, String sku, @PositiveOrZero BigDecimal price,
    @DecimalMin("0") @DecimalMax("100") Double x, @DecimalMin("0") @DecimalMax("100") Double y, @DecimalMin("0.1") @DecimalMax("4") Double scale,
    Boolean active) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/products/{id}/accessories") public List<com.wolfe.visual.AccessoryOption> adminAccessories(@PathVariable Long id, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return accessories.findByProductIdOrderByTypeAscNameAsc(id, org.springframework.data.domain.PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100))).getContent();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PostMapping("/products/{id}/accessories")
    @ResponseStatus(HttpStatus.CREATED) public com.wolfe.visual.AccessoryOption createAccessory(@PathVariable Long id, @Valid @RequestBody AccessoryRequest r) {
        var p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return accessories.save(new com.wolfe.visual.AccessoryOption(p, r.name().trim(), r.type().trim().toUpperCase(), r.overlayUrl().trim(), r.sku(),
        r.price(), r.x() == null?50:r.x(), r.y() == null?50:r.y(), r.scale() == null?1:r.scale(),
        r.active() == null || r.active()));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/products/{id}/accessories/{accessoryId}") public com.wolfe.visual.AccessoryOption updateAccessory(@PathVariable Long id,
    @PathVariable Long accessoryId,
    @Valid @RequestBody AccessoryRequest r) {
        var x = accessories.findById(accessoryId).orElseThrow(() -> new NoSuchElementException("Accessory not found"));
        if (!x.getProduct().getId().equals(id))throw new IllegalArgumentException("Accessory does not belong to product");
        x.update(r.name().trim(), r.type().trim().toUpperCase(), r.overlayUrl().trim(), r.sku(), r.price(), r.x() == null?x.getX():r.x(),
        r.y() == null?x.getY():r.y(), r.scale() == null?x.getScale():r.scale(),
        r.active() == null?x.isActive():r.active());
        return accessories.save(x);
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @DeleteMapping("/products/{id}/accessories/{accessoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteAccessory(@PathVariable Long id, @PathVariable Long accessoryId) {
        var x = accessories.findById(accessoryId).orElseThrow(() -> new NoSuchElementException("Accessory not found"));
        if (!x.getProduct().getId().equals(id))throw new IllegalArgumentException("Accessory does not belong to product");
        x.update(x.getName(), x.getType(), x.getOverlayUrl(), x.getSku(), x.getPrice(), x.getX(), x.getY(), x.getScale(), false);
        accessories.save(x);
    }
    public record OrderAdminView(String id, String supportNumber, Long customerId, String status, long total, long subtotal, long shippingFee,
                                 long discountAmount, String currency, String paymentMethod, String shippingMethod,
                                 String customerName, String customerEmail, String phone, String address, String city,
                                 String pincode, java.time.Instant createdAt) {
        OrderAdminView(Order o) {
            this(o.getId(), o.getSupportNumber(), o.getCustomerId(), o.getStatus(), o.getTotal(), o.getSubtotal(), o.getShippingFee(),
                    o.getDiscountAmount(), o.getCurrency(), o.getPaymentMethod(), o.getShippingMethod(),
                    o.getCustomerName(), o.getCustomerEmail(), o.getPhone(), o.getAddress(), o.getCity(),
                    o.getPincode(), o.getCreatedAt());
        }
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_ORDERS')")
    @GetMapping("/orders") public List<OrderAdminView> orders(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return orders.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
                .getContent().stream().map(OrderAdminView::new).toList();
    }
    public record OrderStatusRequest(@NotBlank String status) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_ORDERS')")
    @PutMapping("/orders/{id}/status") public OrderAdminView updateOrderStatus(@PathVariable String id, @Valid @RequestBody OrderStatusRequest r, org.springframework.security.core.Authentication auth) {
        return new OrderAdminView(orderService.transition(id, r.status(), auth != null ? auth.getName() : "ADMIN"));
    }
    public record OrderHistoryAdminView(Long id, String orderId, String status, String note, String actor, java.time.Instant createdAt) {
        OrderHistoryAdminView(com.wolfe.order.OrderStatusHistory h) { this(h.getId(), h.getOrderId(), h.getStatus(), h.getNote(), h.getActor(), h.getCreatedAt()); }
    }
    public record ReviewAdminView(Long id, Long productId, Long customerId, int rating, String review, String status, java.time.Instant createdAt) {
        ReviewAdminView(com.wolfe.review.ProductReview r) { this(r.getId(), r.getProductId(), r.getCustomerId(), r.getRating(), r.getReview(), r.getStatus(), r.getCreatedAt()); }
    }
    public record QuoteAdminView(Long id, Long customerId, Long productId, Long configurationId, String message, String status, java.time.Instant createdAt) {
        QuoteAdminView(com.wolfe.quote.QuoteRequest q) { this(q.getId(), q.getCustomerId(), q.getProductId(), q.getConfigurationId(), q.getMessage(), q.getStatus(), q.getCreatedAt()); }
    }
    public record CustomDesignAdminView(Long id, Long customerId, String projectName, String requirements, String referenceImageUrl, String status, java.time.Instant createdAt) {
        CustomDesignAdminView(com.wolfe.customdesign.CustomDesignRequest d) { this(d.getId(), d.getCustomerId(), d.getProjectName(), d.getRequirements(), d.getReferenceImageUrl(), d.getStatus(), d.getCreatedAt()); }
    }
    public record ReturnAdminView(Long id, String orderId, Long customerId, String reason, String itemQuantitiesJson, String status, long refundAmount, String refundStatus, String adminNote, java.time.Instant createdAt, java.time.Instant updatedAt, String restockStatus, String settlementAdjustmentStatus, java.time.Instant completedAt) {
        ReturnAdminView(com.wolfe.returning.ReturnRequest r) { this(r.getId(), r.getOrderId(), r.getCustomerId(), r.getReason(), r.getItemQuantitiesJson(), r.getStatus(), r.getRefundAmount(), r.getRefundStatus(), r.getAdminNote(), r.getCreatedAt(), r.getUpdatedAt(), r.getRestockStatus(), r.getSettlementAdjustmentStatus(), r.getCompletedAt()); }
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_ORDERS')")
    @GetMapping("/order-history/{orderId}") public List<OrderHistoryAdminView> orderHistory(@PathVariable String orderId) {
        if (!orders.existsById(orderId))throw new NoSuchElementException("Order not found");
        return history.findByOrderIdOrderByCreatedAtAsc(orderId).stream().map(OrderHistoryAdminView::new).toList();
    }
    public record CustomerView(Long id, String name, String email, String phone, String role) {
        CustomerView(Customer c) {
            this(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getRole());
        }
    }
    public record CustomerAccessUpdate(Boolean enabled, Boolean locked, String role) {}
    @PutMapping("/customers/{id}/access") @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CUSTOMER_ACCESS')")
    public CustomerView updateCustomerAccess(@PathVariable Long id, @Valid @RequestBody CustomerAccessUpdate r, Authentication auth) {
        var actor = auth.getAuthorities().stream().anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
        if (r.role() != null && !actor) throw new org.springframework.security.access.AccessDeniedException("Only SUPER_ADMIN may change customer roles");
        var c = customers.findByIdForUpdate(id).orElseThrow(() -> new NoSuchElementException("Customer not found"));
        if (r.enabled() != null) c.setEnabled(r.enabled());
        if (r.locked() != null) c.setLocked(r.locked());
        if (r.role() != null) {
            String role = r.role().trim().toUpperCase();
            if (!Set.of("CUSTOMER", "RETAILER", "ADMIN", "SUPER_ADMIN").contains(role)) throw new IllegalArgumentException("Invalid customer role");
            c.setRole(role);
        }
        c.incrementSessionVersion();
        customers.save(c);
        return new CustomerView(c);
    }

    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CUSTOMERS_READ')")

    @GetMapping("/customers") public List<CustomerView> customerList(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return customers.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC,
        "name"))).getContent().stream().map(CustomerView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @GetMapping("/reviews") public List<ReviewAdminView> reviews(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return reviews.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))).getContent().stream().map(ReviewAdminView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_CATALOG')")
    @PutMapping("/reviews/{id}/status") public ReviewAdminView reviewStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var r = reviews.findById(id).orElseThrow(() -> new NoSuchElementException("Review not found"));
        String status = body.getOrDefault("status", "PENDING").toUpperCase();
        if (!Set.of("PENDING", "APPROVED", "REJECTED").contains(status))throw new IllegalArgumentException("Invalid review status");
        r.moderate(status);
        return new ReviewAdminView(reviews.save(r));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_ORDERS')")
    @GetMapping("/quotes") public List<QuoteAdminView> quotes(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return quotes.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))).getContent().stream().map(QuoteAdminView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_ORDERS')")
    @PutMapping("/quotes/{id}/status") public QuoteAdminView quoteStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var q = quotes.findById(id).orElseThrow(() -> new NoSuchElementException("Quote not found"));
        String status = body.getOrDefault("status", "NEW").toUpperCase();
        if (!Set.of("NEW", "CONTACTED", "QUOTED", "CLOSED").contains(status))throw new IllegalArgumentException("Invalid quote status");
        q.status(status);
        return new QuoteAdminView(quotes.save(q));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_ORDERS')")
    @GetMapping("/custom-design") public List<CustomDesignAdminView> customDesign(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return customDesigns.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))).getContent().stream().map(CustomDesignAdminView::new).toList();
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_ORDERS')")
    @PutMapping("/custom-design/{id}/status") public CustomDesignAdminView customDesignStatus(@PathVariable Long id,
    @RequestBody Map<String,
    String> body) {
        var d = customDesigns.findById(id).orElseThrow(() -> new NoSuchElementException("Custom design not found"));
        String status = body.getOrDefault("status", "NEW").toUpperCase();
        if (!Set.of("NEW", "CONTACTED", "IN_PROGRESS", "COMPLETED",
        "CLOSED").contains(status))throw new IllegalArgumentException("Invalid custom design status");
        d.status(status);
        return new CustomDesignAdminView(customDesigns.save(d));
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_RETURNS')")
    @GetMapping("/returns") public List<ReturnAdminView> returns(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="50") int size) {
        return returnRequests.findAll(org.springframework.data.domain.PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))).getContent().stream().map(ReturnAdminView::new).toList();
    }
    public record ReturnDecision(@NotBlank String status, @Min(0) long refundAmount, String refundStatus, @Size(max = 1000) String adminNote) {
    }
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('PERM_ADMIN_RETURNS')")
    @PutMapping("/returns/{id}") @Transactional public ReturnAdminView updateReturn(@PathVariable Long id, @Valid @RequestBody ReturnDecision r) {
        var rr = returnRequests.findByIdForUpdate(id).orElseThrow(() -> new NoSuchElementException("Return request not found"));
        String status = r.status().toUpperCase();
        if (!Set.of("PENDING", "APPROVED", "REJECTED", "RECEIVED", "COMPLETED").contains(status))throw new IllegalArgumentException("invalid return status");
        String refund = r.refundStatus() == null?rr.getRefundStatus():r.refundStatus().toUpperCase();
        if (!Set.of("NOT_REQUESTED", "PENDING", "PROCESSING", "REFUNDED",
        "FAILED").contains(refund))throw new IllegalArgumentException("invalid refund status");
        if ("REFUNDED".equals(refund) && !"COMPLETED".equals(status)) {
            throw new IllegalArgumentException("REFUNDED status requires a COMPLETED return");
        }
        var order = orders.findByIdForUpdate(rr.getOrderId()).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (r.refundAmount()>order.getTotal())throw new IllegalArgumentException("refund amount cannot exceed order total");
        if ("REFUNDED".equals(refund)) {
            long maxRefund = returnProcessingService.maxRefundableAmount(rr, order);
            if (r.refundAmount() > maxRefund) {
                throw new IllegalArgumentException("refund amount exceeds the selected return items");
            }
        }
        rr.update(status, r.refundAmount(), refund, r.adminNote());
        var saved = returnRequests.save(rr);
        if ("COMPLETED".equals(status)) {
            saved = returnProcessingService.complete(saved.getId(), r.refundAmount(), "ADMIN");
        }
        notificationService.returnUpdate(saved.getCustomerId(), saved.getOrderId(), saved.getStatus());
        return saved;
    }
    private static boolean allowedTransition(String from, String to, Map<String, Set<String>> transitions) {
        return from.equals(to) || transitions.getOrDefault(from, Set.of()).contains(to);
    }

}
