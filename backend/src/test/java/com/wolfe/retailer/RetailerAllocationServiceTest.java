package com.wolfe.retailer;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.order.Order;
import com.wolfe.order.OrderItem;
import com.wolfe.order.OrderItemRepository;
import com.wolfe.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RetailerAllocationServiceTest {
    private RetailerRepository retailerRepo;
    private RetailerServiceAreaRepository serviceAreaRepo;
    private RetailerInventoryRepository inventoryRepo;
    private RetailerInventoryService inventoryService;
    private RetailerOrderAssignmentRepository assignmentRepo;
    private FulfillmentRepository fulfillmentRepo;
    private RetailerSettlementService settlementService;
    private RetailerAuditLogRepository auditRepo;
    private OrderRepository orderRepo;
    private OrderItemRepository orderItemRepo;
    private ProductRepository productRepo;
    private ProductVariantRepository variantRepo;
    private RetailerAllocationService service;

    @BeforeEach
    void setUp() {
        retailerRepo = mock(RetailerRepository.class);
        serviceAreaRepo = mock(RetailerServiceAreaRepository.class);
        inventoryRepo = mock(RetailerInventoryRepository.class);
        inventoryService = mock(RetailerInventoryService.class);
        assignmentRepo = mock(RetailerOrderAssignmentRepository.class);
        fulfillmentRepo = mock(FulfillmentRepository.class);
        settlementService = mock(RetailerSettlementService.class);
        auditRepo = mock(RetailerAuditLogRepository.class);
        orderRepo = mock(OrderRepository.class);
        orderItemRepo = mock(OrderItemRepository.class);
        productRepo = mock(ProductRepository.class);
        variantRepo = mock(ProductVariantRepository.class);

        service = new RetailerAllocationService(
                retailerRepo, serviceAreaRepo, inventoryRepo, inventoryService,
                assignmentRepo, fulfillmentRepo, settlementService, auditRepo,
                orderRepo, orderItemRepo, productRepo, variantRepo
        );
    }

    @Test
    void testAllocateOrderWhenCandidateEligible() {
        Order order = new Order("WLF-001", 1L, "CONFIRMED", 100000, "INR", "COD", "STANDARD", "John Doe", "john@example.com", "9876543210", "10B Road", "Jodhpur", "342003");
        OrderItem item = new OrderItem("WLF-001", 10L, "Door Handle", 2, 50000, null, null, null, 0, 100L, "WLF-HND-01", "Matt Black", null);

        when(orderRepo.findById("WLF-001")).thenReturn(Optional.of(order));
        when(orderItemRepo.findByOrderId("WLF-001")).thenReturn(List.of(item));

        Retailer r = new Retailer("Hinglaj Hardware", "Owner", "h@wolfe.internal", "999", "Address", "Jodhpur", "Rajasthan", "342003");
        r.setId(1L);
        when(retailerRepo.findByStatusAndVerificationStatus("ACTIVE", "VERIFIED")).thenReturn(List.of(r));
        when(retailerRepo.findById(1L)).thenReturn(Optional.of(r));

        RetailerServiceArea area = new RetailerServiceArea(1L, "342003", "Jodhpur", "Sardarpura", 12);
        when(serviceAreaRepo.findByRetailerId(1L)).thenReturn(List.of(area));

        RetailerInventory inv = new RetailerInventory(1L, 10L, 100L, "WLF-HND-01", 20, 5);
        when(inventoryRepo.findByRetailerIdAndSku(1L, "WLF-HND-01")).thenReturn(Optional.of(inv));

        when(settlementService.calculateLineItemSettlement(any(), any(), any(), any(), anyLong()))
                .thenReturn(new RetailerSettlementService.MarginCalculation(100000, 10000, 90000, "Default"));

        boolean allocated = service.allocateOrder(order, List.of(item));
        assertTrue(allocated);

        // Verify reservation called
        verify(inventoryService, times(1)).reserveStock(eq(1L), eq(10L), eq(100L), eq("WLF-HND-01"), eq(2), eq("WLF-001"), eq("AUTO_ALLOCATION"));
        verify(assignmentRepo, times(1)).save(any());
        verify(fulfillmentRepo, times(1)).save(any());
    }

    @Test
    void testFulfillmentDeliveryFinalizesStockAndMarksSettlementEligible() {
        Fulfillment f = new Fulfillment("WLF-001", 1L);
        f.setStatus("OUT_FOR_DELIVERY");
        when(fulfillmentRepo.findByOrderId("WLF-001")).thenReturn(Optional.of(f));

        OrderItem item = new OrderItem("WLF-001", 10L, "Door Handle", 2, 50000, null, null, null, 0, 100L, "WLF-HND-01", "Matt Black", null);
        when(orderItemRepo.findByOrderId("WLF-001")).thenReturn(List.of(item));

        Order order = new Order("WLF-001", 1L, "SHIPPED", 100000, "INR", "COD", "STANDARD", "John Doe", "john@example.com", "9876543210", "10B Road", "Jodhpur", "342003");
        when(orderRepo.findById("WLF-001")).thenReturn(Optional.of(order));

        service.deliverOrder("WLF-001", 1L);

        assertEquals("DELIVERED", f.getStatus());
        assertEquals("DELIVERED", order.getStatus());
        verify(inventoryService, times(1)).fulfillStock(eq(1L), eq(10L), eq(100L), eq("WLF-HND-01"), eq(2), eq("WLF-001"), eq("RETAILER_1"));
        verify(settlementService, times(1)).markEligible("WLF-001");
    }

    @Test
    void testInvalidStateTransitionsRejected() {
        // 1. Pack order when still ASSIGNED (must be ACCEPTED first)
        Fulfillment f = new Fulfillment("WLF-002", 1L);
        f.setStatus("ASSIGNED");
        when(fulfillmentRepo.findByOrderId("WLF-002")).thenReturn(Optional.of(f));

        assertThrows(IllegalStateException.class, () -> service.packOrder("WLF-002", 1L, "TRK-1", "Wolfe Logistics"));

        // 2. Mark ready when ASSIGNED
        assertThrows(IllegalStateException.class, () -> service.markReady("WLF-002", 1L));

        // 3. Out for delivery when ASSIGNED
        assertThrows(IllegalStateException.class, () -> service.outForDelivery("WLF-002", 1L, "TRK-1"));

        // 4. Deliver when ASSIGNED
        assertThrows(IllegalStateException.class, () -> service.deliverOrder("WLF-002", 1L));
    }

    @Test
    void testCrossTenantFulfillmentUpdateRejected() {
        Fulfillment f = new Fulfillment("WLF-003", 1L);
        f.setStatus("ACCEPTED");
        when(fulfillmentRepo.findByOrderId("WLF-003")).thenReturn(Optional.of(f));

        // Retailer 2 tries to pack Retailer 1's fulfillment
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.packOrder("WLF-003", 2L, "TRK-999", "Wolfe"));
    }

    @Test
    void testAllocationFailsWhenStockInsufficient() {
        Order order = new Order("WLF-004", 1L, "CONFIRMED", 100000, "INR", "COD", "STANDARD", "John Doe", "john@example.com", "9876543210", "10B Road", "Jodhpur", "342003");
        OrderItem item = new OrderItem("WLF-004", 10L, "Door Handle", 5, 50000, null, null, null, 0, 100L, "WLF-HND-01", "Matt Black", null);

        when(orderRepo.findById("WLF-004")).thenReturn(Optional.of(order));
        when(orderItemRepo.findByOrderId("WLF-004")).thenReturn(List.of(item));

        Retailer r = new Retailer("Hinglaj Hardware", "Owner", "h@wolfe.internal", "999", "Address", "Jodhpur", "Rajasthan", "342003");
        r.setId(1L);
        when(retailerRepo.findByStatusAndVerificationStatus("ACTIVE", "VERIFIED")).thenReturn(List.of(r));

        RetailerServiceArea area = new RetailerServiceArea(1L, "342003", "Jodhpur", "Sardarpura", 12);
        when(serviceAreaRepo.findByRetailerId(1L)).thenReturn(List.of(area));

        // Only 2 in available stock, order needs 5
        RetailerInventory inv = new RetailerInventory(1L, 10L, 100L, "WLF-HND-01", 2, 5);
        when(inventoryRepo.findByRetailerIdAndSku(1L, "WLF-HND-01")).thenReturn(Optional.of(inv));

        boolean allocated = service.allocateOrder(order, List.of(item));
        assertFalse(allocated);
        verify(inventoryService, never()).reserveStock(anyLong(), anyLong(), any(), any(), anyInt(), any(), any());
    }
}
