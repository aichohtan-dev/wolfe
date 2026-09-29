package com.wolfe.retailer;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RetailerInventoryServiceTest {
    private RetailerInventoryRepository inventoryRepo;
    private InventoryMovementRepository movementRepo;
    private RetailerAuditLogRepository auditRepo;
    private ProductRepository productRepo;
    private ProductVariantRepository variantRepo;
    private RetailerInventoryService service;

    @BeforeEach
    void setUp() {
        inventoryRepo = mock(RetailerInventoryRepository.class);
        movementRepo = mock(InventoryMovementRepository.class);
        auditRepo = mock(RetailerAuditLogRepository.class);
        productRepo = mock(ProductRepository.class);
        variantRepo = mock(ProductVariantRepository.class);
        service = new RetailerInventoryService(inventoryRepo, movementRepo, auditRepo, productRepo, variantRepo);
    }

    @Test
    void testReservationAndFulfillmentLifecycle() {
        RetailerInventory inv = new RetailerInventory(1L, 10L, 100L, "WLF-TEST-SKU", 20, 5);
        when(inventoryRepo.findByRetailerIdAndSkuForUpdate(1L, "WLF-TEST-SKU")).thenReturn(Optional.of(inv));
        when(inventoryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        // 1. Reserve 5 units
        service.reserveStock(1L, 10L, 100L, "WLF-TEST-SKU", 5, "WLF-ORD-01", "TEST_USER");
        assertEquals(20, inv.getPhysicalStock());
        assertEquals(5, inv.getReservedStock());
        assertEquals(15, inv.getAvailableStock());

        // Verify movement logged
        verify(movementRepo, times(1)).save(argThat(m ->
                "RESERVATION".equals(m.getMovementType()) && m.getQuantityChanged() == -5 && "WLF-ORD-01".equals(m.getOrderId())
        ));

        // 2. Fulfill 5 units
        service.fulfillStock(1L, 10L, 100L, "WLF-TEST-SKU", 5, "WLF-ORD-01", "TEST_USER");
        assertEquals(15, inv.getPhysicalStock());
        assertEquals(0, inv.getReservedStock());
        assertEquals(15, inv.getAvailableStock());

        // Verify fulfillment logged
        verify(movementRepo, times(1)).save(argThat(m ->
                "FULFILLMENT".equals(m.getMovementType()) && m.getQuantityChanged() == -5
        ));
    }

    @Test
    void testCannotReserveMoreThanAvailableStock() {
        RetailerInventory inv = new RetailerInventory(1L, 10L, 100L, "WLF-TEST-SKU", 5, 2);
        when(inventoryRepo.findByRetailerIdAndSkuForUpdate(1L, "WLF-TEST-SKU")).thenReturn(Optional.of(inv));

        assertThrows(IllegalArgumentException.class, () ->
                service.reserveStock(1L, 10L, 100L, "WLF-TEST-SKU", 6, "WLF-ORD-02", "TEST_USER")
        );
    }

    @Test
    void testStockAdjustmentAudit() {
        RetailerInventory inv = new RetailerInventory(1L, 10L, 100L, "WLF-TEST-SKU", 10, 2);
        when(inventoryRepo.findByRetailerIdAndSkuForUpdate(1L, "WLF-TEST-SKU")).thenReturn(Optional.of(inv));
        when(inventoryRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        RetailerInventory updated = service.adjustStock(1L, "WLF-TEST-SKU", 25, "ADMIN_USER", "New shipment arrived");
        assertEquals(25, updated.getPhysicalStock());
        assertEquals(25, updated.getAvailableStock());

        verify(movementRepo, times(1)).save(argThat(m ->
                "ADJUSTMENT".equals(m.getMovementType()) && m.getQuantityChanged() == 15 && m.getNewQuantity() == 25
        ));
    }
}
