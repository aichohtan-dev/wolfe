package com.wolfe.order;

import com.wolfe.bundle.BundleItemRepository;
import com.wolfe.bundle.BundleRepository;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.discount.CouponRepository;
import com.wolfe.discount.CouponService;
import com.wolfe.experience.ConfigurationRepository;
import com.wolfe.inventory.InventoryRepository;
import com.wolfe.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceValidationTest {
    private OrderService service() {
        return new OrderService(
            Mockito.mock(OrderRepository.class),
            Mockito.mock(OrderItemRepository.class),
            Mockito.mock(ProductRepository.class),
            Mockito.mock(ProductVariantRepository.class),
            Mockito.mock(InventoryRepository.class),
            Mockito.mock(CouponService.class),
            Mockito.mock(CouponRepository.class),
            Mockito.mock(OrderStatusHistoryRepository.class),
            Mockito.mock(NotificationService.class),
            Mockito.mock(ConfigurationRepository.class),
            Mockito.mock(BundleRepository.class),
            Mockito.mock(BundleItemRepository.class)
        );
    }

    @Test
    void rejectsOnlinePaymentUntilGatewayIsEnabled() {
        var request = new OrderService.CreateOrder(1L, "Test", "test@example.com", "9999999999", "A", "Jodhpur", "342001",
            "RAZORPAY", "STANDARD", null, java.util.List.of(new OrderService.Item("w-01", 1, null, null, null, null)));
        var ex = assertThrows(IllegalArgumentException.class, () -> service().create(request));
        assertTrue(ex.getMessage().contains("Online payment is not enabled"));
    }

    @Test
    void rejectsInvalidQuantityBeforeDatabaseLookup() {
        var request = new OrderService.CreateOrder(1L, "Test", "test@example.com", "9999999999", "A", "Jodhpur", "342001",
            "COD", "STANDARD", null, java.util.List.of(new OrderService.Item("w-01", 0, null, null, null, null)));
        var ex = assertThrows(IllegalArgumentException.class, () -> service().create(request));
        assertEquals("quantity must be between 1 and 100", ex.getMessage());
    }

    @Test
    void rejectsMissingDeliveryDetails() {
        var request = new OrderService.CreateOrder(null, "", "test@example.com", "9999999999", "A", "Jodhpur", "342001",
            "COD", "STANDARD", null, java.util.List.of(new OrderService.Item("w-01", 1, null, null, null, null)));
        var ex = assertThrows(IllegalArgumentException.class, () -> service().create(request));
        assertEquals("complete customer and delivery details are required", ex.getMessage());
    }
}
