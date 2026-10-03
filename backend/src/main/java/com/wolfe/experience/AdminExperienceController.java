package com.wolfe.experience;

import com.wolfe.catalog.*;
import com.wolfe.visual.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.NoSuchElementException;
import java.util.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/experience")
public class AdminExperienceController {
    public record CartRecoveryView(Long customerId, java.time.Instant lastActivity, boolean reminderSent, java.time.Instant reminderClaimedAt) {
        CartRecoveryView(CartRecovery c) { this(c.getCustomerId(), c.getLastActivity(), c.isReminderSent(), c.getReminderClaimedAt()); }
    }
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
    public record SpinResponse(Long id, Long productId, String imageUrl, int sortOrder) {}
    @GetMapping("/products/{id}/spin") public List<SpinResponse> listSpin(@PathVariable Long id) {
        product(id);
        return spins.findByProductIdOrderBySortOrderAsc(id).stream()
            .map(x -> new SpinResponse(x.getId(), x.getProduct().getId(), x.getImageUrl(), x.getSortOrder())).toList();
    }
    @PostMapping("/products/{id}/spin") public SpinResponse addSpin(@PathVariable Long id, @Valid @RequestBody SpinRequest r) {
        var x = spins.save(new ProductSpinFrame(product(id), r.imageUrl(), r.sortOrder()));
        return new SpinResponse(x.getId(), x.getProduct().getId(), x.getImageUrl(), x.getSortOrder());
    }
    @PutMapping("/spin/{id}") public SpinResponse updateSpin(@PathVariable Long id, @Valid @RequestBody SpinRequest r) {
        var x = spins.findById(id).orElseThrow(() -> new NoSuchElementException("Spin frame not found"));
        x.update(r.imageUrl(), r.sortOrder());
        x = spins.save(x);
        return new SpinResponse(x.getId(), x.getProduct().getId(), x.getImageUrl(), x.getSortOrder());
    }
    @DeleteMapping("/spin/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteSpin(@PathVariable Long id) {
        spins.deleteById(id);
    }
    public record AssetRequest(String modelUrl, String arUrl, String posterUrl, Boolean active) {
    }
    public record AssetResponse(Long id, Long productId, String modelUrl, String arUrl, String posterUrl, boolean active) {}
    @GetMapping("/products/{id}/visual-asset") public AssetResponse getAsset(@PathVariable Long id) {
        product(id);
        var x = assets.findByProductId(id).orElseThrow(() -> new NoSuchElementException("Visual asset not found"));
        return new AssetResponse(x.getId(), x.getProductId(), x.getModelUrl(), x.getArUrl(), x.getPosterUrl(), x.isActive());
    }
    @PutMapping("/products/{id}/visual-asset") public AssetResponse asset(@PathVariable Long id, @RequestBody AssetRequest r) {
        var x = assets.findByProductId(id).orElseGet(() -> new ProductVisualAsset(product(id), r.modelUrl(), r.arUrl(), r.posterUrl(),
        r.active() == null || r.active()));
        x.update(r.modelUrl(), r.arUrl(), r.posterUrl(), r.active() == null || r.active());
        x = assets.save(x);
        return new AssetResponse(x.getId(), x.getProductId(), x.getModelUrl(), x.getArUrl(), x.getPosterUrl(), x.isActive());
    }
    @DeleteMapping("/products/{id}/visual-asset")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteAsset(@PathVariable Long id) {
        product(id);
        assets.findByProductId(id).ifPresent(assets::delete);
    }
    public record HotspotRequest(@NotBlank String label, @NotBlank String targetSlug, double x, double y, Boolean active) {
    }
    public record HotspotResponse(Long id, Long visualContentId, String label, String targetSlug, double x, double y, boolean active) {}
    @GetMapping("/visual/{id}/hotspots") public List<HotspotResponse> listHotspots(@PathVariable Long id) {
        contents.findById(id).orElseThrow(() -> new NoSuchElementException("Visual content not found"));
        return hotspots.findByVisualContentIdAndOrderByIdAsc(id).stream()
            .map(x -> new HotspotResponse(x.getId(), x.getVisualContentId(), x.getLabel(), x.getTargetSlug(), x.getX(), x.getY(), x.isActive())).toList();
    }
    @PostMapping("/visual/{id}/hotspots") public HotspotResponse hotspot(@PathVariable Long id, @Valid @RequestBody HotspotRequest r) {
        var x = hotspots.save(new VisualHotspot(contents.findById(id).orElseThrow(() -> new NoSuchElementException("Visual content not found")), r.label(), r.targetSlug(), clamp(r.x()), clamp(r.y()),
        r.active() == null || r.active()));
        return new HotspotResponse(x.getId(), x.getVisualContentId(), x.getLabel(), x.getTargetSlug(), x.getX(), x.getY(), x.isActive());
    }
    @PutMapping("/hotspots/{id}") public HotspotResponse updateHotspot(@PathVariable Long id, @Valid @RequestBody HotspotRequest r) {
        var x = hotspots.findById(id).orElseThrow(() -> new NoSuchElementException("Hotspot not found"));
        x.update(r.label(), r.targetSlug(), clamp(r.x()), clamp(r.y()), r.active() == null || r.active());
        x = hotspots.save(x);
        return new HotspotResponse(x.getId(), x.getVisualContentId(), x.getLabel(), x.getTargetSlug(), x.getX(), x.getY(), x.isActive());
    }
    @DeleteMapping("/hotspots/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteHotspot(@PathVariable Long id) {
        hotspots.deleteById(id);
    }
    @GetMapping("/back-in-stock") public List<BackInStockSubscription> stockSubscriptions() {
        return stocks.findAll(org.springframework.data.domain.PageRequest.of(0, 100, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"))).getContent();
    }
    @GetMapping("/cart-recovery") public List<CartRecoveryView> cartRecovery() {
        return recovery.findAll(org.springframework.data.domain.PageRequest.of(0, 100, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "lastActivity"))).getContent().stream().map(CartRecoveryView::new).toList();
    }
    private Product product(Long id) {
        return products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
    }
    private double clamp(double v) {
        return Math.max(0, Math.min(100, v));
    }
}
