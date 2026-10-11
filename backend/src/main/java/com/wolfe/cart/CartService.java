package com.wolfe.cart;

import com.wolfe.bundle.Bundle;
import com.wolfe.bundle.BundleItemRepository;
import com.wolfe.bundle.BundleRepository;
import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.experience.ConfigurationService;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final BundleRepository bundles;
    private final BundleItemRepository bundleItems;
    private final ConfigurationService configurations;

    public CartService(CartItemRepository cartItems, ProductRepository products, ProductVariantRepository variants,
                       BundleRepository bundles, BundleItemRepository bundleItems, ConfigurationService configurations) {
        this.cartItems = cartItems;
        this.products = products;
        this.variants = variants;
        this.bundles = bundles;
        this.bundleItems = bundleItems;
        this.configurations = configurations;
    }

    @Transactional
    public CartItem put(Long customerId, String slug, int quantity, Long variantId, Long bundleId, String configurationToken) {
        if (quantity < 1 || quantity > 100) throw new IllegalArgumentException("quantity must be between 1 and 100");
        Product product = products.findBySlugIgnoreCase(slug.trim()).filter(Product::isActive)
                .orElseThrow(() -> new IllegalArgumentException("product not found or inactive"));
        resolveVariant(product, variantId);
        validateBundle(product, bundleId);
        String token = normalizeToken(configurationToken);
        if (token != null) configurations.resolveForCart(token, customerId, product.getId());
        if (cartItems.countByCustomerId(customerId) >= 50 &&
                cartItems.findExact(customerId, product.getId(), variantId, bundleId, token).isEmpty())
            throw new IllegalStateException("Cart cannot contain more than 50 distinct items");
        CartItem item = cartItems.findExact(customerId, product.getId(), variantId, bundleId, token)
                .orElseGet(() -> new CartItem(customerId, product.getId(), variantId, bundleId, token, quantity));
        item.setQuantity(quantity);
        return cartItems.save(item);
    }

    @Transactional
    public void remove(Long customerId, String slug, Long variantId, Long bundleId, String configurationToken) {
        Product product = products.findBySlugIgnoreCase(slug.trim())
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        cartItems.findExact(customerId, product.getId(), variantId, bundleId, normalizeToken(configurationToken))
                .ifPresent(cartItems::delete);
    }

    @Transactional
    public void clear(Long customerId) { cartItems.deleteByCustomerId(customerId); }

    private ProductVariant resolveVariant(Product product, Long variantId) {
        if (variantId == null) return null;
        return variants.findByIdAndActiveTrue(variantId)
                .filter(v -> Objects.equals(v.getProduct().getId(), product.getId()))
                .orElseThrow(() -> new IllegalArgumentException("variant not found, inactive, or does not belong to product"));
    }

    private void validateBundle(Product product, Long bundleId) {
        if (bundleId == null) return;
        Bundle bundle = bundles.findById(bundleId).filter(Bundle::isActive)
                .orElseThrow(() -> new IllegalArgumentException("bundle not found or inactive"));
        if (!bundleItems.findByBundleId(bundleId).stream().anyMatch(x -> Objects.equals(x.getProductId(), product.getId())))
            throw new IllegalArgumentException("product is not part of the selected bundle");
    }

    private static String normalizeToken(String token) {
        return token == null || token.isBlank() ? null : token.trim();
    }
}
