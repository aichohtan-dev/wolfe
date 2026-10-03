package com.wolfe.wishlist;

import com.wolfe.catalog.Product;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static com.wolfe.security.CustomerAccess.requireCustomer;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {
    private final WishlistItemRepository repo;
    private final com.wolfe.catalog.ProductRepository products;
    public WishlistController(WishlistItemRepository repo, com.wolfe.catalog.ProductRepository products) {
        this.repo = repo;
        this.products = products;
    }
    @GetMapping("/{customerId}") public List<com.wolfe.catalog.ProductController.ProductPublicView> get(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        return repo.findByCustomerId(customerId).stream().map(x -> products.findById(x.getProductId()).orElse(null)).filter(Objects::nonNull).filter(Product::isActive).map(com.wolfe.catalog.ProductController.ProductPublicView::new).toList();
    }
    @GetMapping("/{customerId}/slugs") public List<String> getSlugs(@PathVariable Long customerId, Authentication auth) {
        return get(customerId, auth).stream().map(com.wolfe.catalog.ProductController.ProductPublicView::slug).toList();
    }
    @PutMapping("/{customerId}/{slug}") public List<String> add(@PathVariable Long customerId, @PathVariable String slug, Authentication auth) {
        requireCustomer(auth, customerId);
        Long productId = products.findBySlug(slug).filter(Product::isActive).orElseThrow(() -> new IllegalArgumentException("product not found or inactive")).getId();
        repo.save(new WishlistItem(customerId, productId));
        return getSlugs(customerId, auth);
    }
    @DeleteMapping("/{customerId}/{slug}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void remove(@PathVariable Long customerId, @PathVariable String slug, Authentication auth) {
        requireCustomer(auth, customerId);
        Long productId = products.findBySlug(slug).orElseThrow(() -> new IllegalArgumentException("product not found")).getId();
        repo.deleteById(new WishlistItem.Key(customerId, productId));
    }
    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void clear(@PathVariable Long customerId, Authentication auth) {
        requireCustomer(auth, customerId);
        repo.deleteAll(repo.findByCustomerId(customerId));
    }
}
