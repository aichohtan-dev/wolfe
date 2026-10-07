package com.wolfe.experience;

import com.wolfe.catalog.*;
import com.wolfe.security.CustomerAccess;
import com.wolfe.visual.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/experience")
public class ExperienceController {
    private final ProductRepository products;
    private final SpinRepository spins;
    private final VisualAssetRepository assets;
    private final HotspotRepository hotspots;
    private final RecentRepository recent;
    private final StockSubscriptionRepository stocks;
    private final CartRecoveryRepository recovery;
    private final com.wolfe.security.RateLimitService rateLimits;
    private final ConfigurationService configurations;

    public ExperienceController(ProductRepository products, SpinRepository spins, VisualAssetRepository assets, HotspotRepository hotspots,
    RecentRepository recent, StockSubscriptionRepository stocks, CartRecoveryRepository recovery,
    com.wolfe.security.RateLimitService rateLimits, ConfigurationService configurations) {
        this.products = products;
        this.spins = spins;
        this.assets = assets;
        this.hotspots = hotspots;
        this.recent = recent;
        this.stocks = stocks;
        this.recovery = recovery;
        this.rateLimits = rateLimits;
        this.configurations = configurations;
    }

    private Product product(String slug) {
        return products.findBySlug(slug).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }

    public record SpinView(Long id, Long productId, String imageUrl, int sortOrder) {
        SpinView(ProductSpinFrame f) { this(f.getId(), f.getProductId(), f.getImageUrl(), f.getSortOrder()); }
    }
    public record CartRecoveryView(Long customerId, java.time.Instant lastActivity, boolean reminderSent, java.time.Instant reminderClaimedAt) {
        CartRecoveryView(CartRecovery c) { this(c.getCustomerId(), c.getLastActivity(), c.isReminderSent(), c.getReminderClaimedAt()); }
    }
    public record VisualAssetView(Long id, Long productId, String modelUrl, String arUrl, String posterUrl, boolean active) {
        VisualAssetView(ProductVisualAsset a) { this(a.getId(), a.getProductId(), a.getModelUrl(), a.getArUrl(), a.getPosterUrl(), a.isActive()); }
    }

    @GetMapping("/products/{slug}/spin") public List<SpinView> spin(@PathVariable String slug) {
        return spins.findByProductIdOrderBySortOrderAsc(product(slug).getId()).stream().map(SpinView::new).toList();
    }

    @GetMapping("/products/{slug}/visual-asset") public VisualAssetView asset(@PathVariable String slug) {
        return new VisualAssetView(assets.findByProductId(product(slug).getId()).filter(ProductVisualAsset::isActive).orElseThrow(() -> new NoSuchElementException("Visual asset not found")));
    }

    @GetMapping("/visual/{id}/hotspots") public List<VisualHotspot> hotspots(@PathVariable Long id) {
        return hotspots.findByActiveVisualContentId(id);
    }

    public record ConfigRequest(Long customerId, @NotNull Long productId, Long accessoryId, @NotBlank @Size(max = 12000) String configJson) {}

    @PostMapping("/configurations")
    public ProductConfiguration save(@Valid @RequestBody ConfigRequest r, Authentication a, HttpServletRequest request) {
        rateLimits.check("configuration", r.customerId() == null ? "anonymous" : String.valueOf(r.customerId()), clientIp(request));
        if (r.customerId() != null) CustomerAccess.requireCustomer(a, r.customerId());
        return configurations.create(r.customerId(), r.productId(), r.accessoryId(), r.configJson());
    }

    public record SharedConfigurationResponse(Long id, String shareToken, Long productId, Long selectedAccessoryId, long addonPrice, long basePrice, String configJson, java.time.Instant createdAt) {
        SharedConfigurationResponse(ProductConfiguration c, ConfigurationService.CurrentPricing pricing) {
            this(c.getId(), c.getShareToken(), c.getProductId(), c.getSelectedAccessoryId(),
                    pricing.addonPrice(), pricing.basePrice(), c.getConfigJson(), c.getCreatedAt());
        }
    }

    @GetMapping("/configurations/{token}") public SharedConfigurationResponse share(@PathVariable String token) {
        ProductConfiguration c = configurations.resolve(token);
        return new SharedConfigurationResponse(c, configurations.currentPricing(c));
    }


    @PostMapping("/customers/{customerId}/recent/{productId}") public RecentlyViewed viewed(@PathVariable Long customerId, @PathVariable Long productId,
    Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        products.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        var x = recent.findByCustomerIdAndProductId(customerId, productId).orElseGet(() -> new RecentlyViewed(customerId, productId));
        x.touch();
        return recent.save(x);
    }

    @GetMapping("/customers/{customerId}/recent") public List<ProductController.ProductPublicView> recent(@PathVariable Long customerId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        var viewed = recent.findTop8ByCustomerIdOrderByViewedAtDesc(customerId);
        var ids = viewed.stream().map(RecentlyViewed::getProductId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        var productMap = products.findAllByIdInAndActiveTrue(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, java.util.function.Function.identity()));
        return viewed.stream().map(x -> productMap.get(x.getProductId()))
                .filter(Objects::nonNull).map(ProductController.ProductPublicView::new).toList();
    }

    @PostMapping("/customers/{customerId}/back-in-stock/{productId}") public BackInStockSubscription subscribe(@PathVariable Long customerId,
    @PathVariable Long productId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        products.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        var x = stocks.findByCustomerIdAndProductId(customerId, productId).orElseGet(() -> new BackInStockSubscription(customerId, productId));
        x.setActive(true);
        return stocks.save(x);
    }

    @DeleteMapping("/customers/{customerId}/back-in-stock/{productId}") public void unsubscribe(@PathVariable Long customerId, @PathVariable Long productId,
    Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        stocks.findByCustomerIdAndProductId(customerId, productId).ifPresent(x -> {
            x.setActive(false); stocks.save(x);
        });
    }

    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    @PostMapping("/customers/{customerId}/cart-recovery/touch") public CartRecoveryView touchCart(@PathVariable Long customerId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        var x = recovery.findById(customerId).orElseGet(() -> new CartRecovery(customerId));
        x.touch();
        return new CartRecoveryView(recovery.save(x));
    }
}
