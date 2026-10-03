package com.wolfe.retailer;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RetailerInventoryService {
    private final RetailerInventoryRepository inventoryRepo;
    private final InventoryMovementRepository movementRepo;
    private final RetailerAuditLogRepository auditRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;

    public RetailerInventoryService(RetailerInventoryRepository inventoryRepo,
                                  InventoryMovementRepository movementRepo,
                                  RetailerAuditLogRepository auditRepo,
                                  ProductRepository productRepo,
                                  ProductVariantRepository variantRepo) {
        this.inventoryRepo = inventoryRepo;
        this.movementRepo = movementRepo;
        this.auditRepo = auditRepo;
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
    }

    @Transactional
    public RetailerInventory getOrCreateInventory(Long retailerId, Long productId, Long variantId, String sku, int initialStock) {
        Optional<RetailerInventory> existing = inventoryRepo.findByRetailerIdAndSkuForUpdate(retailerId, sku);
        if (existing.isPresent()) {
            return existing.get();
        }

        RetailerInventory item = new RetailerInventory(retailerId, productId, variantId, sku, Math.max(0, initialStock), 5);
        RetailerInventory saved = inventoryRepo.save(item);

        movementRepo.save(new InventoryMovement(
                retailerId, productId, variantId, sku,
                0, initialStock, initialStock,
                "INITIAL", null, "SYSTEM", "Initial stock entry"
        ));

        auditRepo.save(new RetailerAuditLog(
                "RetailerInventory", String.valueOf(saved.getId()),
                "CREATE_STOCK", "SYSTEM", "Created initial inventory SKU " + sku + " with " + initialStock + " units"
        ));

        return saved;
    }

    @Transactional
    public RetailerInventory adjustStock(Long retailerId, String sku, int newPhysicalStock, String actor, String reason) {
        RetailerInventory item = inventoryRepo.findByRetailerIdAndSkuForUpdate(retailerId, sku)
                .orElseThrow(() -> new NoSuchElementException("Inventory not found for SKU: " + sku + " at retailer: " + retailerId));

        int prevStock = item.getPhysicalStock();
        int delta = newPhysicalStock - prevStock;

        item.adjustPhysicalStock(newPhysicalStock);
        RetailerInventory saved = inventoryRepo.save(item);

        movementRepo.save(new InventoryMovement(
                retailerId, item.getProductId(), item.getVariantId(), sku,
                prevStock, delta, newPhysicalStock,
                "ADJUSTMENT", null, actor, reason
        ));

        auditRepo.save(new RetailerAuditLog(
                "RetailerInventory", String.valueOf(saved.getId()),
                "ADJUST_STOCK", actor, "Adjusted SKU " + sku + " from " + prevStock + " to " + newPhysicalStock + " (" + reason + ")"
        ));

        return saved;
    }

    @Transactional
    public void reserveStock(Long retailerId, Long productId, Long variantId, String sku, int quantity, String orderId, String actor) {
        RetailerInventory item = inventoryRepo.findByRetailerIdAndSkuForUpdate(retailerId, sku)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found for SKU: " + sku + " at retailer: " + retailerId));

        int prevAvailable = item.getAvailableStock();
        item.reserve(quantity);
        inventoryRepo.save(item);

        movementRepo.save(new InventoryMovement(
                retailerId, productId, variantId, sku,
                prevAvailable, -quantity, item.getAvailableStock(),
                "RESERVATION", orderId, actor, "Order reservation for " + orderId
        ));

        auditRepo.save(new RetailerAuditLog(
                "RetailerInventory", String.valueOf(item.getId()),
                "RESERVE_STOCK", actor, "Reserved " + quantity + " units of " + sku + " for order " + orderId
        ));
    }

    @Transactional
    public void releaseStock(Long retailerId, Long productId, Long variantId, String sku, int quantity, String orderId, String actor) {
        RetailerInventory item = inventoryRepo.findByRetailerIdAndSkuForUpdate(retailerId, sku)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found for SKU: " + sku + " at retailer: " + retailerId));

        int prevAvailable = item.getAvailableStock();
        item.release(quantity);
        inventoryRepo.save(item);

        movementRepo.save(new InventoryMovement(
                retailerId, productId, variantId, sku,
                prevAvailable, quantity, item.getAvailableStock(),
                "RESERVATION_RELEASE", orderId, actor, "Released reservation for order " + orderId
        ));

        auditRepo.save(new RetailerAuditLog(
                "RetailerInventory", String.valueOf(item.getId()),
                "RELEASE_STOCK", actor, "Released " + quantity + " units of " + sku + " for order " + orderId
        ));
    }

    @Transactional
    public void fulfillStock(Long retailerId, Long productId, Long variantId, String sku, int quantity, String orderId, String actor) {
        RetailerInventory item = inventoryRepo.findByRetailerIdAndSkuForUpdate(retailerId, sku)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found for SKU: " + sku + " at retailer: " + retailerId));

        int prevPhysical = item.getPhysicalStock();
        item.fulfill(quantity);
        inventoryRepo.save(item);

        movementRepo.save(new InventoryMovement(
                retailerId, productId, variantId, sku,
                prevPhysical, -quantity, item.getPhysicalStock(),
                "FULFILLMENT", orderId, actor, "Fulfillment completed for order " + orderId
        ));

        auditRepo.save(new RetailerAuditLog(
                "RetailerInventory", String.valueOf(item.getId()),
                "FULFILL_STOCK", actor, "Fulfilled " + quantity + " units of " + sku + " for order " + orderId
        ));
    }

    @Transactional
    public void restockStock(Long retailerId, Long productId, Long variantId, String sku, int quantity, String orderId, String actor, String reason) {
        RetailerInventory item = inventoryRepo.findByRetailerIdAndSkuForUpdate(retailerId, sku)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found for SKU: " + sku + " at retailer: " + retailerId));
        int prevPhysical = item.getPhysicalStock();
        item.adjustPhysicalStock(Math.addExact(prevPhysical, quantity));
        inventoryRepo.save(item);
        movementRepo.save(new InventoryMovement(retailerId, productId, variantId, sku, prevPhysical, quantity, item.getPhysicalStock(),
                "RETURN_RESTOCK", orderId, actor, reason));
        auditRepo.save(new RetailerAuditLog("RetailerInventory", String.valueOf(item.getId()), "RETURN_RESTOCK", actor,
                "Restocked " + quantity + " units of " + sku + " for return/order " + orderId));
    }

    public List<RetailerInventory> getLowStockAlerts(Long retailerId) {
        return inventoryRepo.findLowStockItems(retailerId);
    }
}
