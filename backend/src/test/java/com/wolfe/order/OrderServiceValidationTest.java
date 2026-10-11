package com.wolfe.order;

import com.wolfe.customer.Customer;
import com.wolfe.customer.CustomerRepository;
import com.wolfe.bundle.BundleItemRepository;
import com.wolfe.bundle.BundleRepository;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.discount.CouponRepository;
import com.wolfe.discount.CouponService;
import com.wolfe.experience.ConfigurationService;
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
            Mockito.mock(ConfigurationService.class),
            Mockito.mock(BundleRepository.class),
            Mockito.mock(BundleItemRepository.class)
        );
    }

    @Test
    void rejectsCheckoutWhenCustomerEmailIsUnverifiedBeforeOrderOrStockMutation() {
        var orders = Mockito.mock(OrderRepository.class);
        var items = Mockito.mock(OrderItemRepository.class);
        var products = Mockito.mock(ProductRepository.class);
        var variants = Mockito.mock(ProductVariantRepository.class);
        var inventory = Mockito.mock(InventoryRepository.class);
        var couponService = Mockito.mock(CouponService.class);
        var coupons = Mockito.mock(CouponRepository.class);
        var history = Mockito.mock(OrderStatusHistoryRepository.class);
        var notifications = Mockito.mock(NotificationService.class);
        var configurations = Mockito.mock(ConfigurationService.class);
        var bundles = Mockito.mock(BundleRepository.class);
        var bundleItems = Mockito.mock(BundleItemRepository.class);
        var accessories = Mockito.mock(com.wolfe.visual.AccessoryOptionRepository.class);
        var retailerAllocation = Mockito.mock(com.wolfe.retailer.RetailerAllocationService.class);
        var customers = Mockito.mock(CustomerRepository.class);

        Customer customer = new Customer("Test", "test@example.com", "encoded");
        customer.markEmailUnverified();
        Mockito.when(customers.findByIdForUpdate(1L)).thenReturn(java.util.Optional.of(customer));

        var service = new OrderService(orders, items, products, variants, inventory, couponService, coupons,
                history, notifications, configurations, bundles, bundleItems, accessories, retailerAllocation, customers);
        var request = new OrderService.CreateOrder(
                1L, "Test", "test@example.com", "9999999999", "A", "Jodhpur", "342001",
                null, null, "COD", "STANDARD", null,
                java.util.List.of(new OrderService.Item("w-01", 1, null, null, null, null)),
                "unverified-email-test-key");

        var ex = assertThrows(IllegalStateException.class, () -> service.create(request));

        assertEquals("email verification is required before placing a COD order", ex.getMessage());
        Mockito.verify(customers).findByIdForUpdate(1L);
        Mockito.verifyNoInteractions(orders, items, products, variants, inventory, couponService, coupons,
                history, notifications, configurations, bundles, bundleItems, accessories, retailerAllocation);
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
