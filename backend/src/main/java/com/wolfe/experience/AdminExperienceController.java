package com.wolfe.experience;

import com.wolfe.catalog.*;
import com.wolfe.visual.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/experience")
public class AdminExperienceController {
    private final ProductRepository products;
    private final SpinRepository spins;
    private final VisualAssetRepository assets;
    private final HotspotRepository hotspots;
    private final VisualContentRepository contents;
    private final StockSubscriptionRepository stocks;
    private final CartRecoveryRepository recovery;
    public AdminExperienceController(ProductRepository products, SpinRepository spins, VisualAssetRepository assets, HotspotRepository hotspots,
    VisualContentRepository contents, StockSubscriptionRepository stocks,
    CartRecoveryRepository recovery) {
        this.products = products;
        this.spins = spins;
        this.assets = assets;
        this.hotspots = hotspots;
        this.contents = contents;
        this.stocks = stocks;
        this.recovery = recovery;
    }
    public record SpinRequest(@NotBlank String imageUrl, @Min(0) int sortOrder) {
    }
    @PostMapping("/products/{id}/spin") public ProductSpinFrame addSpin(@PathVariable Long id, @Valid @RequestBody SpinRequest r) {
        return spins.save(new ProductSpinFrame(product(id), r.imageUrl(), r.sortOrder()));
    }
    @DeleteMapping("/spin/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteSpin(@PathVariable Long id) {
        spins.deleteById(id);
    }
    public record AssetRequest(String modelUrl, String arUrl, String posterUrl, Boolean active) {
    }
    @PutMapping("/products/{id}/visual-asset") public ProductVisualAsset asset(@PathVariable Long id, @RequestBody AssetRequest r) {
        var x = assets.findByProductId(id).orElseGet(() -> new ProductVisualAsset(product(id), r.modelUrl(), r.arUrl(), r.posterUrl(),
        r.active() == null || r.active()));
        x.update(r.modelUrl(), r.arUrl(), r.posterUrl(), r.active() == null || r.active());
        return assets.save(x);
    }
    public record HotspotRequest(@NotBlank String label, @NotBlank String targetSlug, double x, double y, Boolean active) {
    }
    @PostMapping("/visual/{id}/hotspots") public VisualHotspot hotspot(@PathVariable Long id, @Valid @RequestBody HotspotRequest r) {
        return hotspots.save(new VisualHotspot(contents.findById(id).orElseThrow(), r.label(), r.targetSlug(), clamp(r.x()), clamp(r.y()),
        r.active() == null || r.active()));
    }
    @DeleteMapping("/hotspots/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteHotspot(@PathVariable Long id) {
        hotspots.deleteById(id);
    }
    @GetMapping("/back-in-stock") public List<BackInStockSubscription> stockSubscriptions() {
        return stocks.findAll();
    }
    @GetMapping("/cart-recovery") public List<CartRecovery> cartRecovery() {
        return recovery.findAll();
    }
    private Product product(Long id) {
        return products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }
    private double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }
}
