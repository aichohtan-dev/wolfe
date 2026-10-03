package com.wolfe.experience;

import com.wolfe.catalog.*;
import com.wolfe.security.CustomerAccess;
import com.wolfe.visual.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/experience")
public class ExperienceController {
    @org.springframework.beans.factory.annotation.Value("${WOLFE_CONFIGURATION_TTL_DAYS:30}")
    private int configurationTtlDays;
    private final ProductRepository products;
    private final SpinRepository spins;
    private final VisualAssetRepository assets;
    private final HotspotRepository hotspots;
    private final ConfigurationRepository configs;
    private final RecentRepository recent;
    private final StockSubscriptionRepository stocks;
    private final CartRecoveryRepository recovery;
    private final AccessoryOptionRepository accessories;
    private final com.wolfe.security.RateLimitService rateLimits;
    private final ObjectMapper objectMapper;
    public ExperienceController(ProductRepository products, SpinRepository spins, VisualAssetRepository assets, HotspotRepository hotspots,
    ConfigurationRepository configs, RecentRepository recent, StockSubscriptionRepository stocks, CartRecoveryRepository recovery,
    AccessoryOptionRepository accessories, com.wolfe.security.RateLimitService rateLimits, ObjectMapper objectMapper) {
        this.products = products;
        this.spins = spins;
        this.assets = assets;
        this.hotspots = hotspots;
        this.configs = configs;
        this.recent = recent;
        this.stocks = stocks;
        this.recovery = recovery;
        this.accessories = accessories;
        this.rateLimits = rateLimits;
        this.objectMapper = objectMapper;
    }
    private Product product(String slug) {
        return products.findBySlug(slug).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }
    private String sanitizeConfigJson(String raw) {
        try {
            JsonNode node = objectMapper.readTree(raw);
            if (!node.isObject()) throw new IllegalArgumentException("configuration must be a JSON object");
            ObjectNode source = (ObjectNode) node;
            Set<String> allowed = Set.of("accessoryId", "room", "accessorySku", "accessoryName");
            var fields = source.fieldNames();
            while (fields.hasNext()) {
                String name = fields.next();
                if (!allowed.contains(name)) throw new IllegalArgumentException("unsupported configuration field: " + name);
            }
            if (source.has("accessoryId") && !source.get("accessoryId").isNull() && !source.get("accessoryId").canConvertToLong())
                throw new IllegalArgumentException("accessoryId must be numeric");
            if (source.has("room") && !source.get("room").isNull() && (!source.get("room").isTextual() || !Set.of("light", "warm", "dark").contains(source.get("room").asText())))
                throw new IllegalArgumentException("room is invalid");
            for (String field : List.of("accessorySku", "accessoryName")) {
                if (source.has(field) && !source.get(field).isNull() && (!source.get(field).isTextual() || source.get(field).asText().length() > 200))
                    throw new IllegalArgumentException(field + " is invalid");
            }
            return objectMapper.writeValueAsString(source);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("configuration JSON is invalid");
        }
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
    public record ConfigRequest(Long customerId, @NotNull Long productId, Long accessoryId, @NotBlank @Size(max = 12000) String configJson) {
    }
    @PostMapping("/configurations")
    @Transactional public ProductConfiguration save(@Valid @RequestBody ConfigRequest r, Authentication a, HttpServletRequest request) {
        rateLimits.check("configuration", r.customerId() == null ? "anonymous" : String.valueOf(r.customerId()), clientIp(request));
        var product = products.findById(r.productId()).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
        if (r.customerId() != null)CustomerAccess.requireCustomer(a, r.customerId());
        Long accessoryId = null;
        long addon = 0;
        if (r.accessoryId() != null) {
            var accessory = accessories.findById(r.accessoryId()).filter(AccessoryOption::isActive).orElseThrow(() -> new NoSuchElementException("Accessory not found"));
            if (!accessory.getProduct().getId().equals(product.getId()))throw new IllegalArgumentException("Accessory does not belong to product");
            accessoryId = accessory.getId();
            addon = accessory.getPrice() == null?0:accessory.getPrice().movePointRight(2).longValueExact();
            JsonNode normalized = parseConfigJson(r.configJson());
            if (normalized.has("accessoryId") && !normalized.get("accessoryId").isNull() && normalized.get("accessoryId").asLong() != accessoryId) {
                throw new IllegalArgumentException("configuration accessoryId does not match selected accessory");
            }
        } else {
            JsonNode normalized = parseConfigJson(r.configJson());
            if (normalized.has("accessoryId") && !normalized.get("accessoryId").isNull()) throw new IllegalArgumentException("configuration accessoryId requires selected accessory");
        }
        long base = product.getPrice().movePointRight(2).longValueExact();
        return configs.save(new ProductConfiguration(UUID.randomUUID().toString().replace("-", ""), r.customerId(), r.productId(), accessoryId,
        addon, base, sanitizeConfigJson(r.configJson())));
    }
    public record SharedConfigurationResponse(Long id, String shareToken, Long productId, Long selectedAccessoryId, long addonPrice, long basePrice, String configJson, java.time.Instant createdAt) {
        SharedConfigurationResponse(ProductConfiguration c, long currentBasePrice, long currentAddonPrice) {
            this(c.getId(), c.getShareToken(), c.getProductId(), c.getSelectedAccessoryId(), currentAddonPrice, currentBasePrice, c.getConfigJson(), c.getCreatedAt());
        }
    }

    @GetMapping("/configurations/{token}") public SharedConfigurationResponse share(@PathVariable String token) {
        if (token == null || !token.matches("[A-Za-z0-9]{16,64}")) throw new NoSuchElementException("Configuration not found");
        var c = configs.findByShareToken(token).orElseThrow(() -> new NoSuchElementException("Configuration not found"));
        if (c.getCreatedAt().plus(java.time.Duration.ofDays(configurationTtlDays)).isBefore(java.time.Instant.now()))
            throw new NoSuchElementException("Configuration not found");
        var product = products.findById(c.getProductId()).filter(Product::isActive).orElseThrow(() -> new NoSuchElementException("Product not found"));
        long currentBase = product.getPrice().movePointRight(2).longValueExact();
        long currentAddon = 0;
        if (c.getSelectedAccessoryId() != null) {
            var accessory = accessories.findById(c.getSelectedAccessoryId()).filter(AccessoryOption::isActive)
                    .filter(a -> a.getProduct().getId().equals(product.getId()))
                    .orElseThrow(() -> new NoSuchElementException("Configuration accessory not found"));
            currentAddon = accessory.getPrice() == null ? 0 : accessory.getPrice().movePointRight(2).longValueExact();
        }
        return new SharedConfigurationResponse(c, currentBase, currentAddon);
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
    /** The API is private behind the trusted Nginx proxy; do not trust client-supplied forwarding headers. */
    private String clientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    @PostMapping("/customers/{customerId}/cart-recovery/touch") public CartRecoveryView touchCart(@PathVariable Long customerId, Authentication a) {
        CustomerAccess.requireCustomer(a, customerId);
        var x = recovery.findById(customerId).orElseGet(() -> new CartRecovery(customerId));
        x.touch();
        return new CartRecoveryView(recovery.save(x));
    }

    private com.fasterxml.jackson.databind.JsonNode parseConfigJson(String configJson) {
        try {
            return objectMapper.readTree(sanitizeConfigJson(configJson));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid configuration JSON", e);
        }
    }
}
