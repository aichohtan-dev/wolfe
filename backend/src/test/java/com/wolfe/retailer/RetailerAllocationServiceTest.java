package com.wolfe.retailer;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariant;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.order.Order;
import com.wolfe.order.OrderItem;
import com.wolfe.order.OrderItemRepository;
import com.wolfe.order.OrderRepository;
import com.wolfe.inventory.InventoryRepository;
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
    private InventoryRepository globalInventoryRepo;
    private RetailerAuditWriter auditWriter;
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
        globalInventoryRepo = mock(InventoryRepository.class);
        auditWriter = mock(RetailerAuditWriter.class);

        service = new RetailerAllocationService(
                retailerRepo, serviceAreaRepo, inventoryRepo, inventoryService,
                assignmentRepo, fulfillmentRepo, settlementService, auditRepo,
                orderRepo, orderItemRepo, productRepo, variantRepo, globalInventoryRepo, auditWriter
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
        when(serviceAreaRepo.findByPincodeAndActiveTrue("342003")).thenReturn(List.of(area));

        RetailerInventory inv = new RetailerInventory(1L, 10L, 100L, "WLF-HND-01", 20, 5);
        when(inventoryRepo.findByRetailerIdAndSku(1L, "WLF-HND-01")).thenReturn(Optional.of(inv));
        when(inventoryRepo.findByRetailerIdAndSkuIn(eq(1L), anyCollection())).thenReturn(List.of(inv));

        when(orderRepo.findById("WLF-001")).thenReturn(Optional.of(order));
        when(orderRepo.findByIdForUpdate("WLF-001")).thenReturn(Optional.of(order));

        when(settlementService.calculateLineItemSettlement(any(), any(), any(), any(), anyLong()))
                .thenReturn(new RetailerSettlementService.MarginCalculation(100000, 10000, 90000, "Default"));

        RetailerSettlement mockSettlement = new RetailerSettlement("WLF-001", 1L, 100000, 10000, 90000);
        mockSettlement.setId(10L);
        when(settlementService.initializeSettlement(any(), any(), anyLong(), anyLong(), anyLong())).thenReturn(mockSettlement);

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
        when(fulfillmentRepo.findByOrderIdForUpdate("WLF-001")).thenReturn(Optional.of(f));

        OrderItem item = new OrderItem("WLF-001", 10L, "Door Handle", 2, 50000, null, null, null, 0, 100L, "WLF-HND-01", "Matt Black", null);
        when(orderItemRepo.findByOrderId("WLF-001")).thenReturn(List.of(item));

        Order order = new Order("WLF-001", 1L, "SHIPPED", 100000, "INR", "COD", "STANDARD", "John Doe", "john@example.com", "9876543210", "10B Road", "Jodhpur", "342003");
        when(orderRepo.findById("WLF-001")).thenReturn(Optional.of(order));
        when(orderRepo.findByIdForUpdate("WLF-001")).thenReturn(Optional.of(order));

        service.deliverOrder("WLF-001", 1L);

        assertEquals("DELIVERED", f.getStatus());
        assertEquals("DELIVERED", order.getStatus());
        verify(inventoryService, times(1)).fulfillStock(eq(1L), eq(10L), eq(100L), eq("WLF-HND-01"), eq(2), eq("WLF-001"), eq("RETAILER_1"));
        verify(settlementService, times(1)).markEligible("WLF-001", 1L);
    }

    @Test
    void testInvalidStateTransitionsRejected() {
        Order order = new Order("WLF-002", 1L, "CONFIRMED", 100000, "INR", "COD", "STANDARD", "John Doe", "john@example.com", "9876543210", "10B Road", "Jodhpur", "342003");
        when(orderRepo.findById("WLF-002")).thenReturn(Optional.of(order));
        when(orderRepo.findByIdForUpdate("WLF-002")).thenReturn(Optional.of(order));

        // 1. Pack order when still ASSIGNED (must be ACCEPTED first)
        Fulfillment f = new Fulfillment("WLF-002", 1L);
        f.setStatus("ASSIGNED");
        when(fulfillmentRepo.findByOrderId("WLF-002")).thenReturn(Optional.of(f));
        when(fulfillmentRepo.findByOrderIdForUpdate("WLF-002")).thenReturn(Optional.of(f));

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
        Order order = new Order("WLF-003", 1L, "CONFIRMED", 100000, "INR", "COD", "STANDARD", "John Doe", "john@example.com", "9876543210", "10B Road", "Jodhpur", "342003");
        when(orderRepo.findById("WLF-003")).thenReturn(Optional.of(order));
        when(orderRepo.findByIdForUpdate("WLF-003")).thenReturn(Optional.of(order));

        Fulfillment f = new Fulfillment("WLF-003", 1L);
        f.setStatus("ACCEPTED");
        when(fulfillmentRepo.findByOrderId("WLF-003")).thenReturn(Optional.of(f));
        when(fulfillmentRepo.findByOrderIdForUpdate("WLF-003")).thenReturn(Optional.of(f));

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
    @Test
    void reassignmentCreatesNewFulfillmentRowInsteadOfRetargetingHistory() {
        Order order = new Order("WLF-ABA", 1L, "CONFIRMED", 100000, "INR", "COD", "STANDARD", "John Doe", "john@example.com", "9876543210", "10B Road", "Jodhpur", "342003");
        OrderItem item = new OrderItem("WLF-ABA", 10L, "Door Handle", 1, 100000, null, null, null, 0, 100L, "WLF-HND-01", "Matt Black", null);
        Retailer a = new Retailer("Retailer A", "Owner", "a@wolfe.internal", "999", "Address", "Jodhpur", "Rajasthan", "342003");
        a.setId(1L);
        Retailer b = new Retailer("Retailer B", "Owner", "b@wolfe.internal", "998", "Address", "Jodhpur", "Rajasthan", "342003");
        b.setId(2L);
        when(orderRepo.findByIdForUpdate("WLF-ABA")).thenReturn(Optional.of(order));
        when(orderItemRepo.findByOrderId("WLF-ABA")).thenReturn(List.of(item));
        when(retailerRepo.findById(1L)).thenReturn(Optional.of(a));
        when(fulfillmentRepo.findByOrderIdForUpdateRows("WLF-ABA"))
                .thenReturn(List.of(new Fulfillment("WLF-ABA", 2L)));
        when(assignmentRepo.findTopByOrderIdOrderByIdDesc("WLF-ABA"))
                .thenReturn(Optional.of(new RetailerOrderAssignment("WLF-ABA", 2L, "ADMIN", "retry")));
        when(settlementService.calculateLineItemSettlement(anyLong(), anyString(), anyLong(), nullable(Long.class), anyLong()))
                .thenReturn(new RetailerSettlementService.MarginCalculation(100000, 10000, 90000, "Default"));
        when(settlementService.initializeSettlement(anyString(), anyLong(), anyLong(), anyLong(), anyLong()))
                .thenReturn(new RetailerSettlement("WLF-ABA", 1L, 100000, 10000, 90000));

        service.assignToRetailer("WLF-ABA", 1L, "ADMIN", "A again");

        verify(inventoryService).releaseStock(eq(2L), eq(10L), eq(100L), eq("WLF-HND-01"), eq(1), eq("WLF-ABA"), eq("ADMIN"));
        verify(inventoryService).reserveStock(eq(1L), eq(10L), eq(100L), eq("WLF-HND-01"), eq(1), eq("WLF-ABA"), eq("ADMIN"));
        verify(fulfillmentRepo, atLeastOnce()).save(argThat(f -> "WLF-ABA".equals(f.getOrderId()) && Long.valueOf(1L).equals(f.getRetailerId()) && "ASSIGNED".equals(f.getStatus())));
    }

    @Test
    void cancellationAfterRetailerRejectionDoesNotReleaseStockAgain() {
        RetailerOrderAssignment assignment = new RetailerOrderAssignment("WLF-REJ", 1L, "RETAILER_1", "");
        assignment.reject("No stock");
        when(assignmentRepo.findTopByOrderIdOrderByIdDesc("WLF-REJ")).thenReturn(Optional.of(assignment));

        service.cancelAllocation("WLF-REJ");

        verifyNoInteractions(inventoryService);
        assertEquals("CANCELLED", assignment.getStatus());
    }

}
