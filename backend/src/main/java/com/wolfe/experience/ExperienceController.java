package com.wolfe.experience;

import com.wolfe.catalog.*;
import com.wolfe.security.CustomerAccess;
import com.wolfe.visual.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/experience")
public class ExperienceController {
    private final ProductRepository products;
    private final SpinRepository spins;
    private final VisualAssetRepository assets;
    private final HotspotRepository hotspots;
    private final ConfigurationRepository configs;
    private final RecentRepository recent;
    private final StockSubscriptionRepository stocks;
    private final CartRecoveryRepository recovery;
    private final AccessoryOptionRepository accessories;
    public ExperienceController(ProductRepository products, SpinRepository spins, VisualAssetRepository assets, HotspotRepository hotspots,
    ConfigurationRepository configs, RecentRepository recent, StockSubscriptionRepository stocks, CartRecoveryRepository recovery,
    AccessoryOptionRepository accessories) {
        this.products = products;
        this.spins = spins;
        this.assets = assets;
        this.hotspots = hotspots;
        this.configs = configs;
        this.recent = recent;
        this.stocks = stocks;
        this.recovery = recovery;
        this.accessories = accessories;
    }
    private Product product(String slug) {
        return products.findBySlug(slug).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }
    @GetMapping("/products/{slug}/spin") public List<ProductSpinFrame> spin(@PathVariable String slug) {
        return spins.findByProductIdOrderBySortOrderAsc(product(slug).getId());
    }
    @GetMapping("/products/{slug}/visual-asset") public ProductVisualAsset asset(@PathVariable String slug) {
        return assets.findByProductId(product(slug).getId()).orElseThrow(() -> new NoSuchElementException("Visual asset not found"));
    }
    @GetMapping("/visual/{id}/hotspots") public List<VisualHotspot> hotspots(@PathVariable Long id) {
        return hotspots.findByVisualContentIdAndActiveTrueOrderByIdAsc(id);
    }
    public record ConfigRequest(Long customerId, @NotNull Long productId, Long accessoryId, @NotBlank @Size(max = 12000) String configJson) {
    }
    @PostMapping("/configurations")
    @Transactional public ProductConfiguration save(@Valid @RequestBody ConfigRequest r, Authentication a) {
        var product = products.findById(r.productId()).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
        if (r.customerId() != null)CustomerAccess.requireCustomer(a, r.customerId());
        Long accessoryId = null;
        long addon = 0;
        if (r.accessoryId() != null) {
            var accessory = accessories.findById(r.accessoryId()).filter(AccessoryOption::isActive).orElseThrow(() -> new NoSuchElementException("Accessory not found"));
            if (!accessory.getProduct().getId().equals(product.getId()))throw new IllegalArgumentException("Accessory does not belong to product");
            accessoryId = accessory.getId();
            addon = accessory.getPrice() == null?0:accessory.getPrice().movePointRight(2).longValueExact();
        }
        long base = product.getPrice().movePointRight(2).longValueExact();
        return configs.save(new ProductConfiguration(UUID.randomUUID().toString().replace("-", "").substring(0, 20), r.customerId(), r.productId(), accessoryId,
        addon, base,
        r.configJson()));
    }
    @GetMapping("/configurations/{token}") public ProductConfiguration share(@PathVariable String token) {
        return configs.findByShareToken(token).orElseThrow(() -> new NoSuchElementException("Configuration not found"));
    }
    @PostMapping("/customers/{customerId}/recent/{productId}") public RecentlyViewed viewed(@PathVariable Long customerId, @PathVariable Long productId,
    Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        products.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        var x = recent.findByCustomerIdAndProductId(customerId, productId).orElseGet(() -> new RecentlyViewed(customerId, productId));
        x.touch();
        return recent.save(x);
    }
    @GetMapping("/customers/{customerId}/recent") public List<Product> recent(@PathVariable Long customerId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        return recent.findTop8ByCustomerIdOrderByViewedAtDesc(customerId).stream().map(x -> products.findById(x.getProductId()).orElse(null)).filter(Objects::nonNull).toList();
    }
    @PostMapping("/customers/{customerId}/back-in-stock/{productId}") public BackInStockSubscription subscribe(@PathVariable Long customerId,
    @PathVariable Long productId,
    Authentication a) {
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
        }
        );
    }
    @PostMapping("/customers/{customerId}/cart-recovery/touch") public CartRecovery touchCart(@PathVariable Long customerId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        var x = recovery.findById(customerId).orElseGet(() -> new CartRecovery(customerId));
        x.touch();
        return recovery.save(x);
    }
}
