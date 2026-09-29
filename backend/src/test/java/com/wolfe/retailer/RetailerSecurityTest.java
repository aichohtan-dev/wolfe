package com.wolfe.retailer;

import com.wolfe.order.Order;
import com.wolfe.order.OrderController;
import com.wolfe.order.OrderItem;
import com.wolfe.order.OrderItemRepository;
import com.wolfe.order.OrderRepository;
import com.wolfe.order.OrderService;
import com.wolfe.order.OrderStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RetailerSecurityTest {
    private OrderRepository orderRepo;
    private OrderItemRepository itemRepo;
    private com.wolfe.catalog.ProductRepository productRepo;
    private OrderService orderService;
    private OrderStatusHistoryRepository historyRepo;
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        orderRepo = mock(OrderRepository.class);
        itemRepo = mock(OrderItemRepository.class);
        productRepo = mock(com.wolfe.catalog.ProductRepository.class);
        orderService = mock(OrderService.class);
        historyRepo = mock(OrderStatusHistoryRepository.class);
        orderController = new OrderController(orderRepo, itemRepo, productRepo, orderService, historyRepo);
    }

    @Test
    void testCustomerOrderResponseNeverLeaksRetailerIdentity() {
        Order order = new Order("WLF-SEC-01", 42L, "CONFIRMED", 840000, "INR", "COD", "STANDARD", "Alice Customer", "alice@example.com", "9988776655", "10B Road", "Jodhpur", "342003");
        OrderItem item = new OrderItem("WLF-SEC-01", 10L, "Door Handle", 2, 420000, null, null, null, 0, 101L, "WLF-HND-01-MATT", "Matt Black 150mm", "{}");

        when(orderRepo.findById("WLF-SEC-01")).thenReturn(Optional.of(order));
        when(itemRepo.findByOrderId("WLF-SEC-01")).thenReturn(List.of(item));

        var auth = new UsernamePasswordAuthenticationToken("alice@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        auth.setDetails(42L);

        Map<String, Object> response = orderController.get("WLF-SEC-01", auth);

        assertNotNull(response);
        assertTrue(response.containsKey("order"));
        assertTrue(response.containsKey("items"));

        // Verify NO retailer keys leaked in root or order map
        assertFalse(response.containsKey("retailer"));
        assertFalse(response.containsKey("retailerId"));
        assertFalse(response.containsKey("retailerName"));
        assertFalse(response.containsKey("margin"));
        assertFalse(response.containsKey("settlement"));
        assertFalse(response.containsKey("retailerPayable"));

        Order returnedOrder = (Order) response.get("order");
        assertEquals("CONFIRMED", returnedOrder.getStatus());
        assertEquals(840000, returnedOrder.getTotal());
    }
}
