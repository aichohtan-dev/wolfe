package com.wolfe.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wolfe.bundle.BundleItemRepository;
import com.wolfe.bundle.BundleRepository;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.discount.CouponRepository;
import com.wolfe.discount.CouponService;
import com.wolfe.experience.ConfigurationRepository;
import com.wolfe.inventory.InventoryRepository;
import com.wolfe.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orders;
    @Mock OrderItemRepository items;
    @Mock ProductRepository products;
    @Mock ProductVariantRepository variants;
    @Mock InventoryRepository inventory;
    @Mock CouponService couponService;
    @Mock CouponRepository coupons;
    @Mock OrderStatusHistoryRepository history;
    @Mock NotificationService notifications;
    @Mock ConfigurationRepository configurations;
    @Mock BundleRepository bundles;
    @Mock BundleItemRepository bundleItems;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(
                orders,
                items,
                products,
                variants,
                inventory,
                couponService,
                coupons,
                history,
                notifications,
                configurations,
                bundles,
                bundleItems);
    }

    @Test
    void standardShippingIsFreeAtPostDiscountThreshold() {
        assertEquals(0L, service.shippingFee(250_000L, "STANDARD"));
    }

    @Test
    void standardShippingCosts199BelowThreshold() {
        assertEquals(19_900L, service.shippingFee(249_999L, "STANDARD"));
    }

    @Test
    void blankShippingMethodDefaultsToStandard() {
        assertEquals(19_900L, service.shippingFee(1L, "  "));
    }

    @Test
    void unsupportedShippingMethodIsRejected() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.shippingFee(1L, "EXPRESS"));

        assertEquals("unsupported shipping method: EXPRESS", error.getMessage());
    }
}
