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
    public AdminController(com.wolfe.review.ProductReviewRepository reviews, com.wolfe.quote.QuoteRequestRepository quotes,
    com.wolfe.customdesign.CustomDesignRequestRepository customDesigns, ProductRepository products, com.wolfe.discount.CouponRepository coupons,
    ProductVariantRepository variants, ProductCategoryRepository categoriesRepo, ProductCollectionRepository collectionsRepo,
    ProductMediaRepository mediaRepo, InventoryRepository inventory, OrderRepository orders, CustomerRepository customers,
    com.wolfe.returning.ReturnRequestRepository returnRequests, com.wolfe.notification.CustomerNotificationRepository notifications,
    com.wolfe.order.OrderStatusHistoryRepository history, com.wolfe.notification.NotificationService notificationService,
    com.wolfe.order.OrderService orderService, com.wolfe.visual.VisualContentRepository visualContents,
    com.wolfe.visual.AccessoryOptionRepository accessories, com.wolfe.experience.StockSubscriptionRepository stockSubscriptions,
    com.wolfe.bundle.BundleRepository bundles,
    com.wolfe.bundle.BundleItemRepository bundleItems) {
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
    }
    @GetMapping("/dashboard") public Map<String, Object> dashboard() {
        var allOrders = orders.findAll();
        long revenue = allOrders.stream().filter(o -> !"CANCELLED".equals(o.getStatus())).mapToLong(Order::getTotal).sum();
        long low = inventory.findAll().stream().filter(i -> i.getAvailable() <= 5).count();
        return Map.of("products", products.count(), "activeProducts", products.findAll().stream().filter(Product::isActive).count(), "orders", allOrders.size(),
        "customers", customers.count(), "inventoryItems", inventory.count(), "lowStock", low, "reviews", reviews.count(), "pendingReviews",
        reviews.findAll().stream().filter(r -> "PENDING".equals(r.getStatus())).count(), "quotes", quotes.count(), "openQuotes",
        quotes.findAll().stream().filter(q -> !"CLOSED".equals(q.getStatus())).count(), "customDesigns", customDesigns.count(), "openCustomDesigns",
        customDesigns.findAll().stream().filter(q -> !Set.of("COMPLETED", "CLOSED").contains(q.getStatus())).count(), "returns",
        returnRequests.count(), "revenue", revenue, "confirmed", allOrders.stream().filter(o -> "CONFIRMED".equals(o.getStatus())).count(),
        "processing", allOrders.stream().filter(o -> "PROCESSING".equals(o.getStatus())).count(), "shipped",
        allOrders.stream().filter(o -> "SHIPPED".equals(o.getStatus())).count(), "delivered",
        allOrders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count());
    }
    public record CouponRequest(@NotBlank String code, @NotBlank String discountType, @Positive long value, @Min(0) long minimumSubtotal,
    @Min(0) long maximumDiscount, @Min(1) Integer usageLimit, Boolean active, java.time.Instant startsAt,
    java.time.Instant expiresAt) {
    }
    @GetMapping("/coupons") public List<com.wolfe.discount.Coupon> coupons() {
        return coupons.findAll(org.springframework.data.domain.Sort.by("code"));
    }
    @PostMapping("/coupons")
    @ResponseStatus(HttpStatus.CREATED) public com.wolfe.discount.Coupon createCoupon(@Valid @RequestBody CouponRequest r) {
        if (coupons.findByCode(r.code().trim().toUpperCase()).isPresent())throw new IllegalArgumentException("coupon code already exists");
        var c = new com.wolfe.discount.Coupon();
        c.configure(r.code(), r.discountType(), r.value(), r.minimumSubtotal(), r.maximumDiscount(), r.usageLimit(), r.active() == null || r.active(), r.startsAt(),
        r.expiresAt());
        return coupons.save(c);
    }
    @PutMapping("/coupons/{id}") public com.wolfe.discount.Coupon updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest r) {
        var c = coupons.findById(id).orElseThrow(() -> new NoSuchElementException("Coupon not found"));
        if (coupons.findByCode(r.code().trim().toUpperCase()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("coupon code already exists");
        c.configure(r.code(), r.discountType(), r.value(), r.minimumSubtotal(), r.maximumDiscount(), r.usageLimit(), r.active() == null?c.isActive():r.active(),
        r.startsAt(),
        r.expiresAt());
        return coupons.save(c);
    }
    @DeleteMapping("/coupons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteCoupon(@PathVariable Long id) {
        var c = coupons.findById(id).orElseThrow(() -> new NoSuchElementException("Coupon not found"));
        c.configure(c.getCode(), c.getDiscountType(), c.getValue(), c.getMinimumSubtotal(), c.getMaximumDiscount(), c.getUsageLimit(), false, c.getStartsAt(),
        c.getExpiresAt());
        coupons.save(c);
    }
    public record BundleRequest(@NotBlank String slug, @NotBlank String name, String description, @NotBlank String discountType,
    @PositiveOrZero BigDecimal discountValue, Boolean active,
    @NotEmpty List<Long> productIds) {
    }
    @GetMapping("/bundles") public List<Map<String, Object>> bundles() {
        return bundles.findAll().stream().map(b -> Map.of("id", b.getId(), "slug", b.getSlug(), "name", b.getName(), "description",
        b.getDescription() == null?"":b.getDescription(), "discountType", b.getDiscountType(), "discountValue", b.getDiscountValue(), "active", b.isActive(),
        "productIds",
        bundleItems.findByBundleId(b.getId()).stream().map(com.wolfe.bundle.BundleItem::getProductId).toList())).toList();
    }
    @PostMapping("/bundles")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED) public Map<String, Object> createBundle(@Valid @RequestBody BundleRequest r) {
        if (bundles.findBySlug(r.slug().trim()).isPresent())throw new IllegalArgumentException("bundle slug already exists");
        validateBundleProducts(r.productIds());
        var b = bundles.save(new com.wolfe.bundle.Bundle(r.slug(), r.name(), r.description(), r.discountType(), r.discountValue(),
        r.active() == null || r.active()));
        for (Long pid:new LinkedHashSet<>(r.productIds()))bundleItems.save(new com.wolfe.bundle.BundleItem(b.getId(), pid, 1));
        return Map.of("id", b.getId(), "slug", b.getSlug());
    }
    @PutMapping("/bundles/{id}")
    @Transactional public Map<String, Object> updateBundle(@PathVariable Long id, @Valid @RequestBody BundleRequest r) {
        var b = bundles.findById(id).orElseThrow(() -> new NoSuchElementException("Bundle not found"));
        validateBundleProducts(r.productIds());
        if (bundles.findBySlug(r.slug().trim()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("bundle slug already exists");
        b.configure(r.slug(), r.name(), r.description(), r.discountType(), r.discountValue(), r.active() == null?b.isActive():r.active());
        bundles.save(b);
        bundleItems.deleteByBundleId(id);
        for (Long pid:new LinkedHashSet<>(r.productIds()))bundleItems.save(new com.wolfe.bundle.BundleItem(id, pid, 1));
        return Map.of("id", b.getId(), "slug", b.getSlug());
    }
    @DeleteMapping("/bundles/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteBundle(@PathVariable Long id) {
        var b = bundles.findById(id).orElseThrow(() -> new NoSuchElementException("Bundle not found"));
        b.configure(b.getSlug(), b.getName(), b.getDescription(), b.getDiscountType(), b.getDiscountValue(), false);
        bundles.save(b);
    }
    private void validateBundleProducts(List<Long> ids) {
        if (ids == null || ids.size()<2 || ids.size()>10)throw new IllegalArgumentException("bundle must contain 2-10 products");
        for (Long id:new LinkedHashSet<>(ids)) {
            var p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found: "+id));
            if (!p.isActive())throw new IllegalArgumentException("bundle product must be active: "+p.getSlug());
        }
    }
    public record ProductRequest(@NotBlank String slug, @NotBlank String name, @Positive BigDecimal price, @NotBlank String category, @NotBlank String finish,
    @NotBlank String material, @NotBlank String color, @NotBlank String style, String description, String imageUrl, String mediaUrls, Boolean active,
    Boolean featured,
    Integer sortOrder) {
    }
    public record VariantRequest(@NotBlank String optionName, @NotBlank String optionValue, String sku, @Positive BigDecimal priceOverride, Boolean active) {
    }
    @GetMapping("/products") public List<Product> productList() {
        return products.findAllByOrderBySortOrderAscNameAsc();
    }
    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED) public Product createProduct(@Valid @RequestBody ProductRequest r) {
        ensureCategory(r.category());
        if (products.findBySlug(r.slug()).isPresent())throw new IllegalArgumentException("slug already exists");
        Product p = new Product(r.slug().trim(), r.name().trim(), r.price(), r.category().trim(), r.finish().trim(), r.description());
        p.setImageUrl(r.imageUrl());
        p.setMediaUrls(r.mediaUrls());
        p.setMaterial(r.material().trim());
        p.setColor(r.color().trim());
        p.setStyle(r.style().trim());
        p.setActive(r.active() == null || r.active());
        p.setFeatured(r.featured() != null && r.featured());
        p.setSortOrder(r.sortOrder() == null?0:r.sortOrder());
        p = products.save(p);
        inventory.save(new Inventory(p, 0));
        return p;
    }
    @PutMapping("/products/{id}") public Product updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest r) {
        ensureCategory(r.category());
        Product p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        if (products.findBySlug(r.slug()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("slug already exists");
        p.update(r.name().trim(), r.price(), r.category().trim(), r.finish().trim(), r.description(), r.imageUrl(), r.mediaUrls(),
        r.active() == null?p.isActive():r.active(), r.featured() == null?p.isFeatured():r.featured(),
        r.sortOrder() == null?p.getSortOrder():r.sortOrder());
        p.setMaterial(r.material().trim());
        p.setColor(r.color().trim());
        p.setStyle(r.style().trim());
        return products.save(p);
    }
    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteProduct(@PathVariable Long id) {
        Product p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        p.setActive(false);
        products.save(p);
    }
    public record BulkProductRequest(@NotEmpty List<Long> ids, Boolean active, Boolean featured, Integer sortOrderDelta) {
    }
    @PostMapping("/products/bulk")
    @Transactional public List<Product> bulkProducts(@Valid @RequestBody BulkProductRequest r) {
        var result = new ArrayList<Product>();
        for (Long id:r.ids()) {
            Product p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found: "+id));
            if (r.active() != null)p.setActive(r.active());
            if (r.featured() != null)p.setFeatured(r.featured());
            if (r.sortOrderDelta() != null)p.setSortOrder(Math.max(0, p.getSortOrder()+r.sortOrderDelta()));
            result.add(products.save(p));
        }
        return result;
    }
    @GetMapping("/products/{id}/variants") public List<ProductVariantView> variants(@PathVariable Long id) {
        products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return variants.findByProductIdAndActiveTrueOrderByOptionNameAscOptionValueAsc(id).stream().map(ProductVariantView::new).toList();
    }
    @PostMapping("/products/{id}/variants")
    @ResponseStatus(HttpStatus.CREATED) public ProductVariantView createVariant(@PathVariable Long id, @Valid @RequestBody VariantRequest r) {
        var p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        var v = variants.save(new ProductVariant(p, r.optionName().trim(), r.optionValue().trim(), r.sku() == null?null:r.sku().trim(), r.priceOverride(),
        r.active() == null || r.active()));
        return new ProductVariantView(v);
    }
    @PutMapping("/products/{id}/variants/{variantId}") public ProductVariantView updateVariant(@PathVariable Long id, @PathVariable Long variantId,
    @Valid @RequestBody VariantRequest r) {
        var v = variants.findById(variantId).orElseThrow(() -> new NoSuchElementException("Variant not found"));
        if (!v.getProduct().getId().equals(id))throw new IllegalArgumentException("Variant does not belong to product");
        v.update(r.optionName().trim(), r.optionValue().trim(), r.sku() == null?null:r.sku().trim(), r.priceOverride(), r.active() == null || r.active());
        return new ProductVariantView(variants.save(v));
    }
    @DeleteMapping("/products/{id}/variants/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteVariant(@PathVariable Long id, @PathVariable Long variantId) {
        var v = variants.findById(variantId).orElseThrow(() -> new NoSuchElementException("Variant not found"));
        if (!v.getProduct().getId().equals(id))throw new IllegalArgumentException("Variant does not belong to product");
        v.update(v.getOptionName(), v.getOptionValue(), v.getSku(), v.getPriceOverride(), false);
        variants.save(v);
    }
    public record ProductVariantView(Long id, String optionName, String optionValue, String sku, BigDecimal priceOverride, boolean active) {
        ProductVariantView(ProductVariant v) {
            this(v.getId(), v.getOptionName(), v.getOptionValue(), v.getSku(), v.getPriceOverride(), v.isActive());
        }
    }
    @GetMapping("/categories") public List<ProductCategory> categories() {
        return categoriesRepo.findAllByOrderByNameAsc();
    }
    public record CategoryRequest(@NotBlank String name, Boolean active) {
    }
    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED) public ProductCategory createCategory(@Valid @RequestBody CategoryRequest r) {
        if (categoriesRepo.findByNameIgnoreCase(r.name().trim()).isPresent())throw new IllegalArgumentException("category already exists");
        return categoriesRepo.save(new ProductCategory(r.name().trim()));
    }
    @PutMapping("/categories/{id}") public ProductCategory updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest r) {
        var c = categoriesRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Category not found"));
        if (categoriesRepo.findByNameIgnoreCase(r.name().trim()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("category already exists");
        c.update(r.name().trim(), r.active() == null?c.isActive():r.active());
        return categoriesRepo.save(c);
    }
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
    @GetMapping("/collections")
    @Transactional(readOnly = true) public List<CollectionView> collections() {
        return collectionsRepo.findAllByOrderByNameAsc().stream().map(CollectionView::new).toList();
    }
    @PostMapping("/collections")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional public CollectionView createCollection(@Valid @RequestBody CollectionRequest r) {
        if (collectionsRepo.findByNameIgnoreCase(r.name().trim()).isPresent())throw new IllegalArgumentException("collection already exists");
        var c = new ProductCollection(r.name().trim(), r.description());
        c.update(r.name().trim(), r.description(), r.active() == null || r.active());
        applyProducts(c, r.productIds());
        return new CollectionView(collectionsRepo.save(c));
    }
    @PutMapping("/collections/{id}")
    @Transactional public CollectionView updateCollection(@PathVariable Long id, @Valid @RequestBody CollectionRequest r) {
        var c = collectionsRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Collection not found"));
        if (collectionsRepo.findByNameIgnoreCase(r.name().trim()).filter(x -> !x.getId().equals(id)).isPresent())throw new IllegalArgumentException("collection already exists");
        c.update(r.name().trim(), r.description(), r.active() == null?c.isActive():r.active());
        applyProducts(c, r.productIds());
        return new CollectionView(collectionsRepo.save(c));
    }
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
    @GetMapping("/products/{id}/media") public List<MediaView> media(@PathVariable Long id) {
        products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return mediaRepo.findByProductIdOrderBySortOrderAscIdAsc(id).stream().map(MediaView::new).toList();
    }
    @PostMapping("/products/{id}/media")
    @ResponseStatus(HttpStatus.CREATED) public MediaView createMedia(@PathVariable Long id, @Valid @RequestBody MediaRequest r) {
        var p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        String type = r.type().trim().toUpperCase();
        if (!Set.of("IMAGE", "VIDEO").contains(type))throw new IllegalArgumentException("media type must be IMAGE or VIDEO");
        return new MediaView(mediaRepo.save(new ProductMedia(p, type, r.url().trim(), r.altText(), r.sortOrder() == null?0:r.sortOrder(),
        r.active() == null || r.active())));
    }
    @PutMapping("/products/{id}/media/{mediaId}") public MediaView updateMedia(@PathVariable Long id, @PathVariable Long mediaId,
    @Valid @RequestBody MediaRequest r) {
        var m = mediaRepo.findById(mediaId).orElseThrow(() -> new NoSuchElementException("Media not found"));
        if (!m.getProduct().getId().equals(id))throw new IllegalArgumentException("Media does not belong to product");
        String type = r.type().trim().toUpperCase();
        if (!Set.of("IMAGE", "VIDEO").contains(type))throw new IllegalArgumentException("media type must be IMAGE or VIDEO");
        m.update(type, r.url().trim(), r.altText(), r.sortOrder() == null?m.getSortOrder():r.sortOrder(), r.active() == null?m.isActive():r.active());
        return new MediaView(mediaRepo.save(m));
    }
    @DeleteMapping("/products/{id}/media/{mediaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteMedia(@PathVariable Long id, @PathVariable Long mediaId) {
        var m = mediaRepo.findById(mediaId).orElseThrow(() -> new NoSuchElementException("Media not found"));
        if (!m.getProduct().getId().equals(id))throw new IllegalArgumentException("Media does not belong to product");
        mediaRepo.delete(m);
    }
    @GetMapping("/inventory") public List<InventoryView> inventory() {
        return inventory.findAll().stream().map(i -> new InventoryView(i.getProduct().getId(), i.getProduct().getSlug(), i.getProduct().getName(),
        i.getQuantity(), i.getReserved(),
        i.getAvailable())).toList();
    }
    public record InventoryView(Long productId, String slug, String name, int quantity, int reserved, int available) {
    }
    public record StockRequest(@Min(0) int quantity) {
    }
    @PutMapping("/inventory/{productId}") public InventoryView updateStock(@PathVariable Long productId, @Valid @RequestBody StockRequest r) {
        var p = products.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        var i = inventory.findByProductId(productId).orElseGet(() -> inventory.save(new Inventory(p, 0)));
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
    @GetMapping("/stock-alerts") public List<InventoryView> stockAlerts() {
        return inventory.findAll().stream().filter(i -> i.getAvailable() <= 5).map(i -> new InventoryView(i.getProduct().getId(), i.getProduct().getSlug(),
        i.getProduct().getName(), i.getQuantity(), i.getReserved(),
        i.getAvailable())).toList();
    }
    public record VisualContentRequest(@NotBlank String placement, @NotBlank String title, String subtitle, @NotBlank String mediaType,
    @NotBlank String mediaUrl, String posterUrl, String linkUrl, Boolean active,
    Integer sortOrder) {
    }
    @GetMapping("/visual-content") public List<com.wolfe.visual.VisualContent> visualContent() {
        return visualContents.findAllByOrderByPlacementAscSortOrderAscIdAsc();
    }
    @PostMapping("/visual-content")
    @ResponseStatus(HttpStatus.CREATED) public com.wolfe.visual.VisualContent createVisualContent(@Valid @RequestBody VisualContentRequest r) {
        return visualContents.save(new com.wolfe.visual.VisualContent(r.placement().trim().toUpperCase(), r.title().trim(), r.subtitle(),
        r.mediaType().trim().toUpperCase(), r.mediaUrl().trim(), r.posterUrl(), r.linkUrl(), r.active() == null || r.active(),
        r.sortOrder() == null?0:r.sortOrder()));
    }
    @PutMapping("/visual-content/{id}") public com.wolfe.visual.VisualContent updateVisualContent(@PathVariable Long id,
    @Valid @RequestBody VisualContentRequest r) {
        var x = visualContents.findById(id).orElseThrow(() -> new NoSuchElementException("Visual content not found"));
        x.update(r.placement().trim().toUpperCase(), r.title().trim(), r.subtitle(), r.mediaType().trim().toUpperCase(), r.mediaUrl().trim(), r.posterUrl(),
        r.linkUrl(), r.active() == null?x.isActive():r.active(),
        r.sortOrder() == null?x.getSortOrder():r.sortOrder());
        return visualContents.save(x);
    }
    @DeleteMapping("/visual-content/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteVisualContent(@PathVariable Long id) {
        visualContents.deleteById(id);
    }
    public record AccessoryRequest(@NotBlank String name, @NotBlank String type, @NotBlank String overlayUrl, String sku, @PositiveOrZero BigDecimal price,
    @DecimalMin("0") @DecimalMax("100") Double x, @DecimalMin("0") @DecimalMax("100") Double y, @DecimalMin("0.1") @DecimalMax("4") Double scale,
    Boolean active) {
    }
    @GetMapping("/products/{id}/accessories") public List<com.wolfe.visual.AccessoryOption> adminAccessories(@PathVariable Long id) {
        products.findById(id).orElseThrow();
        return accessories.findByProductIdOrderByTypeAscNameAsc(id);
    }
    @PostMapping("/products/{id}/accessories")
    @ResponseStatus(HttpStatus.CREATED) public com.wolfe.visual.AccessoryOption createAccessory(@PathVariable Long id, @Valid @RequestBody AccessoryRequest r) {
        var p = products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return accessories.save(new com.wolfe.visual.AccessoryOption(p, r.name().trim(), r.type().trim().toUpperCase(), r.overlayUrl().trim(), r.sku(),
        r.price(), r.x() == null?50:r.x(), r.y() == null?50:r.y(), r.scale() == null?1:r.scale(),
        r.active() == null || r.active()));
    }
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
    @DeleteMapping("/products/{id}/accessories/{accessoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteAccessory(@PathVariable Long id, @PathVariable Long accessoryId) {
        var x = accessories.findById(accessoryId).orElseThrow();
        if (!x.getProduct().getId().equals(id))throw new IllegalArgumentException("Accessory does not belong to product");
        x.update(x.getName(), x.getType(), x.getOverlayUrl(), x.getSku(), x.getPrice(), x.getX(), x.getY(), x.getScale(), false);
        accessories.save(x);
    }
    @GetMapping("/orders") public List<Order> orders() {
        return orders.findAllByOrderByCreatedAtDesc();
    }
    public record OrderStatusRequest(@NotBlank String status) {
    }
    @PutMapping("/orders/{id}/status") public Order updateOrderStatus(@PathVariable String id, @Valid @RequestBody OrderStatusRequest r) {
        return orderService.transition(id, r.status());
    }
    @GetMapping("/order-history/{orderId}") public List<com.wolfe.order.OrderStatusHistory> orderHistory(@PathVariable String orderId) {
        if (!orders.existsById(orderId))throw new NoSuchElementException("Order not found");
        return history.findByOrderIdOrderByCreatedAtAsc(orderId);
    }
    public record CustomerView(Long id, String name, String email, String phone, String role) {
        CustomerView(Customer c) {
            this(c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getRole());
        }
    }
    @GetMapping("/customers") public List<CustomerView> customerList() {
        return customers.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC,
        "name")).stream().map(CustomerView::new).toList();
    }
    @GetMapping("/reviews") public List<com.wolfe.review.ProductReview> reviews() {
        return reviews.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }
    @PutMapping("/reviews/{id}/status") public com.wolfe.review.ProductReview reviewStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var r = reviews.findById(id).orElseThrow();
        String status = body.getOrDefault("status", "PENDING").toUpperCase();
        if (!Set.of("PENDING", "APPROVED", "REJECTED").contains(status))throw new IllegalArgumentException("Invalid review status");
        r.moderate(status);
        return reviews.save(r);
    }
    @GetMapping("/quotes") public List<com.wolfe.quote.QuoteRequest> quotes() {
        return quotes.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }
    @PutMapping("/quotes/{id}/status") public com.wolfe.quote.QuoteRequest quoteStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var q = quotes.findById(id).orElseThrow();
        String status = body.getOrDefault("status", "NEW").toUpperCase();
        if (!Set.of("NEW", "CONTACTED", "QUOTED", "CLOSED").contains(status))throw new IllegalArgumentException("Invalid quote status");
        q.status(status);
        return quotes.save(q);
    }
    @GetMapping("/custom-design") public List<com.wolfe.customdesign.CustomDesignRequest> customDesign() {
        return customDesigns.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }
    @PutMapping("/custom-design/{id}/status") public com.wolfe.customdesign.CustomDesignRequest customDesignStatus(@PathVariable Long id,
    @RequestBody Map<String,
    String> body) {
        var d = customDesigns.findById(id).orElseThrow();
        String status = body.getOrDefault("status", "NEW").toUpperCase();
        if (!Set.of("NEW", "CONTACTED", "IN_PROGRESS", "COMPLETED",
        "CLOSED").contains(status))throw new IllegalArgumentException("Invalid custom design status");
        d.status(status);
        return customDesigns.save(d);
    }
    @GetMapping("/returns") public List<com.wolfe.returning.ReturnRequest> returns() {
        return returnRequests.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }
    public record ReturnDecision(@NotBlank String status, @Min(0) long refundAmount, String refundStatus, @Size(max = 1000) String adminNote) {
    }
    @PutMapping("/returns/{id}") public com.wolfe.returning.ReturnRequest updateReturn(@PathVariable Long id, @Valid @RequestBody ReturnDecision r) {
        var rr = returnRequests.findById(id).orElseThrow(() -> new NoSuchElementException("Return request not found"));
        String status = r.status().toUpperCase();
        if (!Set.of("PENDING", "APPROVED", "REJECTED", "RECEIVED", "COMPLETED").contains(status))throw new IllegalArgumentException("invalid return status");
        String refund = r.refundStatus() == null?rr.getRefundStatus():r.refundStatus().toUpperCase();
        if (!Set.of("NOT_REQUESTED", "PENDING", "PROCESSING", "REFUNDED",
        "FAILED").contains(refund))throw new IllegalArgumentException("invalid refund status");
        var order = orders.findById(rr.getOrderId()).orElseThrow(() -> new NoSuchElementException("Order not found"));
        if (r.refundAmount()>order.getTotal())throw new IllegalArgumentException("refund amount cannot exceed order total");
        rr.update(status, r.refundAmount(), refund, r.adminNote());
        var saved = returnRequests.save(rr);
        notificationService.returnUpdate(saved.getCustomerId(), saved.getOrderId(), saved.getStatus());
        return saved;
    }
}
