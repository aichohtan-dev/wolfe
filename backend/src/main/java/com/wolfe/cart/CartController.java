package com.wolfe.cart;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static com.wolfe.security.CustomerAccess.requireCustomer;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {
    private final CartItemRepository repo;
    private final com.wolfe.catalog.ProductRepository products;
    public CartController(CartItemRepository repo, com.wolfe.catalog.ProductRepository products) {
        this.repo = repo;
        this.products = products;
    }
    @GetMapping("/{customerId}") public List<CartItem> get(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        return repo.findByCustomerId(customerId);
    }
    @PutMapping("/{customerId}/{slug}") public CartItem put(@PathVariable Long customerId, @PathVariable String slug,
    @RequestParam(defaultValue = "1") int quantity,
    Authentication auth) {
        requireCustomer(auth, customerId);
        Long productId = products.findBySlugIgnoreCase(slug.trim()).filter(com.wolfe.catalog.Product::isActive).orElseThrow(() -> new IllegalArgumentException("product not found or inactive")).getId();
        if (quantity<1) throw new IllegalArgumentException("quantity must be positive");
        var item = repo.findById(new CartItem.Key(customerId, productId)).orElse(new CartItem(customerId, productId, quantity));
        item.setQuantity(quantity);
        return repo.save(item);
    }
    @DeleteMapping("/{customerId}/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long customerId, @PathVariable String slug, Authentication auth) {
        requireCustomer(auth, customerId);
        Long productId = products.findBySlugIgnoreCase(slug.trim()).filter(com.wolfe.catalog.Product::isActive).orElseThrow(() -> new IllegalArgumentException("product not found or inactive")).getId();
        repo.deleteById(new CartItem.Key(customerId, productId));
    }
}
