package com.wolfe.cart;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.bundle.Bundle;
import com.wolfe.bundle.BundleRepository;
import com.wolfe.bundle.BundleItemRepository;
import com.wolfe.experience.ConfigurationService;
import com.wolfe.experience.ProductConfiguration;
import com.wolfe.security.CustomerAccess;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartItemRepository repo;
    private final CartService cartService;
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final BundleRepository bundles;
    private final BundleItemRepository bundleItems;
    private final ConfigurationService configurations;
    private final com.wolfe.inventory.InventoryRepository inventory;
    private final com.wolfe.visual.AccessoryOptionRepository accessories;

    @org.springframework.beans.factory.annotation.Autowired
    public CartController(CartItemRepository repo, ProductRepository products, ProductVariantRepository variants,
                          BundleRepository bundles, BundleItemRepository bundleItems, ConfigurationService configurations,
                          com.wolfe.inventory.InventoryRepository inventory,
                          com.wolfe.visual.AccessoryOptionRepository accessories,
                          CartService cartService) {
        this.repo = repo;
        this.products = products;
        this.variants = variants;
        this.bundles = bundles;
        this.bundleItems = bundleItems;
        this.configurations = configurations;
        this.inventory = inventory;
        this.accessories = accessories;
        this.cartService = cartService;
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable Long customerId, Authentication auth) {
        CustomerAccess.requireCustomer(auth, customerId);
        cartService.clear(customerId);
    }

    @GetMapping("/{customerId}")
    public List<CartItemView> get(@PathVariable Long customerId, Authentication auth) {
        CustomerAccess.requireCustomer(auth, customerId);
        var rows = repo.findByCustomerId(customerId);
        var productIds = rows.stream().map(CartItem::getProductId).collect(java.util.stream.Collectors.toSet());
        var variantIds = rows.stream().map(CartItem::getVariantId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        var bundleIds = rows.stream().map(CartItem::getBundleId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        var productMap = products.findAllById(productIds).stream().collect(java.util.stream.Collectors.toMap(Product::getId, java.util.function.Function.identity()));
        var variantMap = variants.findAllById(variantIds).stream().collect(java.util.stream.Collectors.toMap(ProductVariant::getId, java.util.function.Function.identity()));
        var bundleMap = bundles.findAllById(bundleIds).stream().collect(java.util.stream.Collectors.toMap(Bundle::getId, java.util.function.Function.identity()));
        var bundleItemMap = bundleItems.findByBundleIdIn(bundleIds).stream().collect(java.util.stream.Collectors.groupingBy(com.wolfe.bundle.BundleItem::getBundleId));
        var inventoryMap = inventory == null ? Map.<Long, com.wolfe.inventory.Inventory>of() : inventory.findAllById(productIds).stream().collect(java.util.stream.Collectors.toMap(com.wolfe.inventory.Inventory::getProductId, java.util.function.Function.identity()));
        var configTokens = rows.stream().map(CartItem::getConfigurationToken).filter(java.util.Objects::nonNull).map(String::trim).filter(v -> !v.isBlank()).collect(java.util.stream.Collectors.toSet());
        var configMap = new HashMap<String, com.wolfe.experience.ProductConfiguration>();
        for (String token : configTokens) {
            try {
                var cfg = configurations.resolve(token);
                configMap.put(cfg.getShareToken(), cfg);
            } catch (NoSuchElementException ignored) {
                // Stale/expired configuration is represented as unavailable in the cart view.
            }
        }
        var accessoryIds = configMap.values().stream().map(com.wolfe.experience.ProductConfiguration::getSelectedAccessoryId).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        var accessoryMap = accessories == null ? Map.<Long, com.wolfe.visual.AccessoryOption>of() : accessories.findAllById(accessoryIds).stream().collect(java.util.stream.Collectors.toMap(com.wolfe.visual.AccessoryOption::getId, java.util.function.Function.identity()));
        return rows.stream().map(i -> view(i, productMap, variantMap, bundleMap, bundleItemMap, inventoryMap, configMap, accessoryMap)).toList();
    }

    @PutMapping("/{customerId}/{slug}")
    public CartItemView put(@PathVariable Long customerId, @PathVariable String slug,
                            @RequestParam(defaultValue = "1") int quantity,
                            @RequestParam(required = false) Long variantId,
                            @RequestParam(required = false) Long bundleId,
                            @RequestParam(required = false) String configurationToken,
                            Authentication auth) {
        CustomerAccess.requireCustomer(auth, customerId);
        return view(cartService.put(customerId, slug, quantity, variantId, bundleId, configurationToken));
    }

    @DeleteMapping("/{customerId}/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long customerId, @PathVariable String slug,
                       @RequestParam(required = false) Long variantId,
                       @RequestParam(required = false) Long bundleId,
                       @RequestParam(required = false) String configurationToken,
                       Authentication auth) {
        CustomerAccess.requireCustomer(auth, customerId);
        cartService.remove(customerId, slug, variantId, bundleId, configurationToken);
    }

    private CartItemView view(CartItem item) { return view(item, null, null, null, null, null, null, null); }

    private CartItemView view(CartItem item, Map<Long, Product> productMap, Map<Long, ProductVariant> variantMap, Map<Long, Bundle> bundleMap, Map<Long, List<com.wolfe.bundle.BundleItem>> bundleItemMap, Map<Long, com.wolfe.inventory.Inventory> inventoryMap, Map<String, com.wolfe.experience.ProductConfiguration> configMap, Map<Long, com.wolfe.visual.AccessoryOption> accessoryMap) {
        Product product = productMap == null ? products.findById(item.getProductId()).orElse(null) : productMap.get(item.getProductId());
        ProductVariant variant = item.getVariantId() == null ? null : (variantMap == null ? variants.findById(item.getVariantId()).orElse(null) : variantMap.get(item.getVariantId()));
        Bundle bundle = item.getBundleId() == null ? null : (bundleMap == null ? bundles.findById(item.getBundleId()).orElse(null) : bundleMap.get(item.getBundleId()));
        Integer bundleBaseQuantity = null;
        Integer bundleUnits = null;
        if (bundle != null) {
            bundleBaseQuantity = (bundleItemMap == null ? bundleItems.findByBundleId(bundle.getId()) : bundleItemMap.getOrDefault(bundle.getId(), List.of())).stream()
                    .filter(b -> Objects.equals(b.getProductId(), item.getProductId()))
                    .map(com.wolfe.bundle.BundleItem::getQuantity).findFirst().orElse(null);
            if (bundleBaseQuantity != null && bundleBaseQuantity > 0 && item.getQuantity() % bundleBaseQuantity == 0) {
                bundleUnits = item.getQuantity() / bundleBaseQuantity;
            }
        }
        return new CartItemView(item.getId(), item.getCustomerId(), item.getProductId(),
                product == null ? null : product.getSlug(), product == null ? null : product.getName(),
                item.getVariantId(), variant == null ? null : variant.getSku(), variant == null ? null : variant.getTitle(),
                variant == null ? null : variant.getColor(), variant == null ? null : variant.getMaterial(),
                variant == null ? null : variant.getSize(), variant == null ? null : variant.getFinish(),
                variant == null ? null : variant.getImageUrl(), variant == null ? null : variant.getPrice(),
                item.getBundleId(), bundle == null ? null : bundle.getSlug(), bundleUnits, bundleBaseQuantity,
                item.getConfigurationToken(), item.getQuantity(), effectiveUnitPrice(product, variant, item.getConfigurationToken(), item.getCustomerId(), configMap, accessoryMap), availableQuantity(item, variant, inventoryMap), configurationAddonPaise(item.getConfigurationToken(), item.getCustomerId(), configMap));
    }

    private java.math.BigDecimal effectiveUnitPrice(Product product, ProductVariant variant, String token, Long customerId, Map<String, com.wolfe.experience.ProductConfiguration> configMap, Map<Long, com.wolfe.visual.AccessoryOption> accessoryMap) {
        java.math.BigDecimal base = variant != null && variant.getPrice() != null ? variant.getPrice() : product == null ? java.math.BigDecimal.ZERO : product.getPrice();
        if (token == null || token.isBlank() || configurations == null || product == null) return base;
        var cfg = configMap == null ? null : configMap.get(token.trim());
        if (cfg == null || !Objects.equals(cfg.getProductId(), product.getId()) || (cfg.getCustomerId()!=null && !Objects.equals(cfg.getCustomerId(), customerId))) return base;
        long addonPaise = configurations.currentAddonPrice(cfg, product.getId());
        return base.add(java.math.BigDecimal.valueOf(addonPaise,2));
    }
    private Integer availableQuantity(CartItem item, ProductVariant variant, Map<Long, com.wolfe.inventory.Inventory> inventoryMap) {
        if (variant != null && variant.getStockQuantity() >= 0) return variant.getStockQuantity();
        if (inventory == null) return null;
        return inventoryMap == null ? inventory.findByProductId(item.getProductId()).map(com.wolfe.inventory.Inventory::getAvailable).orElse(0) : java.util.Optional.ofNullable(inventoryMap.get(item.getProductId())).map(com.wolfe.inventory.Inventory::getAvailable).orElse(0);
    }
    private Long configurationAddonPaise(String token, Long customerId, Map<String, com.wolfe.experience.ProductConfiguration> configMap) {
        if (token == null || token.isBlank() || configurations == null) return 0L;
        var cfg = configMap == null ? null : configMap.get(token.trim());
        return cfg != null && (cfg.getCustomerId()==null || Objects.equals(cfg.getCustomerId(),customerId)) ? cfg.getAddonPrice() : 0L;
    }

    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record CartItemView(Long id, Long customerId, Long productId, String slug, String productName,
                               Long variantId, String variantSku, String variantTitle, String variantColor,
                               String variantMaterial, String variantSize, String variantFinish, String variantImage,
                               java.math.BigDecimal variantPrice, Long bundleId, String bundleSlug, Integer bundleUnits,
                               Integer bundleBaseQuantity, String configurationToken, int quantity, java.math.BigDecimal effectiveUnitPrice, Integer availableQuantity, Long configurationAddonPaise) {}
}
