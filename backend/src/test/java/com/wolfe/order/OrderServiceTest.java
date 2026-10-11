package com.wolfe.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.wolfe.bundle.BundleItemRepository;
import com.wolfe.bundle.BundleRepository;
import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.catalog.ProductVariantRepository;
import com.wolfe.discount.Coupon;
import com.wolfe.discount.CouponRepository;
import com.wolfe.discount.CouponService;
import com.wolfe.experience.ConfigurationService;
import com.wolfe.inventory.Inventory;
import com.wolfe.inventory.InventoryRepository;
import com.wolfe.notification.NotificationService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    @Mock ConfigurationService configurations;
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

    @Test
    void duplicateIdenticalLinesMergeBeforeCouponDiscountAllocationAndPersistence() {
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(11L);
        when(product.getSlug()).thenReturn("widget");
        when(product.getName()).thenReturn("Widget");
        when(product.getPrice()).thenReturn(new BigDecimal("100.00"));
        when(product.isActive()).thenReturn(true);
        when(products.findBySlugIgnoreCase("widget")).thenReturn(Optional.of(product));

        Inventory stock = mock(Inventory.class);
        when(inventory.findByProductIdForUpdate(11L)).thenReturn(Optional.of(stock));

        Coupon coupon = mock(Coupon.class);
        when(coupon.getCode()).thenReturn("SAVE");
        when(couponService.requireForOrder("SAVE", 30_000L, 1L, 0L)).thenReturn(coupon);
        when(couponService.calculate(coupon, 30_000L)).thenReturn(5_000L);
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new OrderService.CreateOrder(
                1L, "Test Customer", "test@example.com", "9999999999",
                "1 Main Road", "Jodhpur", "342001", "COD", "STANDARD", "SAVE",
                List.of(
                        new OrderService.Item("widget", 1, null, null, null, null),
                        new OrderService.Item("widget", 2, null, null, null, null)));

        Order saved = service.create(request);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);
        verify(items, times(1)).save(itemCaptor.capture());
        OrderItem persistedItem = itemCaptor.getValue();

        assertEquals(3, persistedItem.getQuantity(), "identical input lines must persist as one merged line");
        assertEquals(5_000L, persistedItem.getCouponDiscount(), "coupon discount must be allocated once to the merged line");
        assertEquals(25_000L, persistedItem.getLineNetAmount(), "line net amount must equal gross less coupon discount");
        assertEquals(30_000L, saved.getSubtotal(), "subtotal must include all three units before coupon discount");
        assertEquals(5_000L, saved.getDiscountAmount());
        assertEquals(19_900L, saved.getShippingFee());
        assertEquals(44_900L, saved.getTotal(), "persisted total must reconcile subtotal, discount, and shipping");
        verify(stock).reserve(3);
        verify(inventory).save(stock);
        verify(coupons).save(coupon);
    }
}
