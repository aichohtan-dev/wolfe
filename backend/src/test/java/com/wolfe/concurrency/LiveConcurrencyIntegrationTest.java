package com.wolfe.concurrency;

import com.wolfe.catalog.Product;
import com.wolfe.catalog.ProductRepository;
import com.wolfe.customer.Customer;
import com.wolfe.customer.CustomerRepository;
import com.wolfe.discount.Coupon;
import com.wolfe.discount.CouponRepository;
import com.wolfe.inventory.Inventory;
import com.wolfe.inventory.InventoryRepository;
import com.wolfe.order.Order;
import com.wolfe.order.OrderRepository;
import com.wolfe.security.SessionService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@SpringBootTest
public class LiveConcurrencyIntegrationTest {

    @Autowired private DataSource dataSource;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private SessionService sessionService;
    @Autowired private TransactionTemplate transactionTemplate;

    @Test
    void test1_ConcurrentInventoryReservation_PreventsOverselling() throws Exception {
        // Create product and initialize inventory in DB with 5 units
        Product product = transactionTemplate.execute(status -> {
            Product p = new Product("conc-inv-" + UUID.randomUUID(), "Inv Test Product", BigDecimal.valueOf(100), "Metal", "Brass", "Desc");
            return productRepository.save(p);
        });
        inventoryRepository.insertDefault(product.getId(), 5);

        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Boolean reserved = transactionTemplate.execute(status -> {
                        Inventory inv = inventoryRepository.findByProductIdForUpdate(product.getId()).orElseThrow();
                        if (inv.getAvailable() >= 1) {
                            inv.reserve(1);
                            inventoryRepository.save(inv);
                            return true;
                        }
                        return false;
                    });
                    if (Boolean.TRUE.equals(reserved)) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(5, successCount.get(), "Exactly 5 reservations must succeed for stock of 5");
        assertEquals(5, failCount.get(), "Remaining 5 threads must be rejected due to zero available inventory");

        Inventory finalInv = inventoryRepository.findByProductId(product.getId()).orElseThrow();
        assertEquals(5, finalInv.getReserved());
        assertEquals(0, finalInv.getAvailable());
    }

    @Test
    void test2_ConcurrentCheckout_IdempotencyKey_CreatesSingleOrder() throws Exception {
        Customer customer = transactionTemplate.execute(status ->
                customerRepository.save(new Customer("Idemp Customer", "idemp-" + UUID.randomUUID() + "@example.com", "hash")));

        String idempotencyKey = "idemp-key-" + UUID.randomUUID();
        int numThreads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicatePreventedCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Order created = transactionTemplate.execute(status -> {
                        Optional<Order> existing = orderRepository.findByCustomerIdAndIdempotencyKey(customer.getId(), idempotencyKey);
                        if (existing.isPresent()) {
                            return null;
                        }
                        Order order = new Order("ORD-" + UUID.randomUUID().toString().substring(0, 8),
                                customer.getId(), "CONFIRMED", 5000L, "INR", "UPI", "STANDARD",
                                customer.getName(), customer.getEmail(), "9999999999", "123 Street", "Bangalore", "560001");
                        order.setIdempotencyKey(idempotencyKey);
                        return orderRepository.saveAndFlush(order);
                    });
                    if (created != null) {
                        successCount.incrementAndGet();
                    } else {
                        duplicatePreventedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    duplicatePreventedCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, successCount.get(), "Only 1 order must be created for the same idempotency key");
        assertEquals(7, duplicatePreventedCount.get(), "Concurrent requests must detect existing order / unique constraint violation");
    }

    @Test
    void test3_ConcurrentOrderCancellation_PreventsDoubleCancellation() throws Exception {
        Customer customer = transactionTemplate.execute(status ->
                customerRepository.save(new Customer("Cancel Customer", "cancel-" + UUID.randomUUID() + "@example.com", "hash")));

        Order order = transactionTemplate.execute(status -> {
            Order o = new Order("ORD-CANC-" + UUID.randomUUID().toString().substring(0, 8),
                    customer.getId(), "CONFIRMED", 10000L, "INR", "UPI", "STANDARD",
                    customer.getName(), customer.getEmail(), "9999999999", "Address", "City", "560001");
            return orderRepository.save(o);
        });

        int numThreads = 6;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger cancelledCount = new AtomicInteger(0);
        AtomicInteger alreadyCancelledCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    transactionTemplate.execute(status -> {
                        Order o = orderRepository.findByIdForUpdate(order.getId()).orElseThrow();
                        if ("CANCELLED".equals(o.getStatus())) {
                            alreadyCancelledCount.incrementAndGet();
                            return o;
                        }
                        o.setStatus("CANCELLED");
                        orderRepository.save(o);
                        cancelledCount.incrementAndGet();
                        return o;
                    });
                } catch (Exception e) {
                    alreadyCancelledCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, cancelledCount.get(), "Order can only be transitioned to CANCELLED once");
        assertEquals(5, alreadyCancelledCount.get(), "Subsequent cancellation attempts must detect CANCELLED state");
    }

    @Test
    void test4_ConcurrentCouponRedemption_SingleUseEnforced() throws Exception {
        String couponCode = "LIVECONC" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        transactionTemplate.execute(status -> {
            Coupon c = new Coupon();
            c.configure(couponCode, "PERCENT", 10, 0, 10000, 1, 1, true, Instant.now().minusSeconds(60), Instant.now().plus(Duration.ofDays(1)));
            couponRepository.save(c);
            return null;
        });

        int numThreads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger redeemedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    transactionTemplate.execute(status -> {
                        Coupon c = couponRepository.findByCodeForUpdate(couponCode).orElseThrow();
                        if (c.getUsageLimit() != null && c.getUsedCount() >= c.getUsageLimit()) {
                            rejectedCount.incrementAndGet();
                            return null;
                        }
                        c.incrementUsage();
                        couponRepository.save(c);
                        redeemedCount.incrementAndGet();
                        return null;
                    });
                } catch (Exception e) {
                    rejectedCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, redeemedCount.get(), "Coupon with limit 1 must be redeemed exactly once");
        assertEquals(7, rejectedCount.get(), "7 concurrent attempts must be rejected");
    }

    @Test
    void test5_ConcurrentRefreshTokenRotation_DetectsReuseAndRevokesFamily() throws Exception {
        Customer customer = transactionTemplate.execute(status ->
                customerRepository.save(new Customer("Session Customer", "sess-" + UUID.randomUUID() + "@example.com", "hash")));

        SessionService.Session initialSession = sessionService.issue(customer);
        String rawRefreshToken = initialSession.refreshToken();

        // Rotate once legitimately
        SessionService.Session rotated = sessionService.rotate(rawRefreshToken);
        assertNotNull(rotated);

        // Concurrent presentation of the now-revoked old refresh token must trigger InvalidRefreshTokenReuseException
        int numThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger reuseExceptions = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    sessionService.rotate(rawRefreshToken);
                } catch (SessionService.InvalidRefreshTokenReuseException e) {
                    reuseExceptions.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertTrue(reuseExceptions.get() > 0, "At least one thread must catch InvalidRefreshTokenReuseException on reused token");
    }

    @Test
    void test6_ConcurrentCartLineInsert_DatabaseUniqueConstraint() throws Exception {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        long customer = jdbc.queryForObject("insert into customers(name,email,password_hash) values ('conc-cart','conc-cart-'||extract(epoch from clock_timestamp())||'@example.test','x') returning id", Long.class);
        long product = jdbc.queryForObject("insert into products(slug,name,price,category,finish,description) values ('conc-cart-'||extract(epoch from clock_timestamp()),'Conc Cart',10,'Test','Metal','test') returning id", Long.class);

        int numThreads = 6;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger constraintViolationCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    jdbc.update("insert into cart_items(customer_id,product_id,quantity) values (?,?,1)", customer, product);
                    successCount.incrementAndGet();
                } catch (DataIntegrityViolationException e) {
                    constraintViolationCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        try {
            assertEquals(1, successCount.get(), "Exactly 1 cart item row must be inserted");
            assertEquals(5, constraintViolationCount.get(), "5 concurrent duplicate cart inserts must fail unique constraint");
        } finally {
            jdbc.update("delete from cart_items where customer_id=?", customer);
            jdbc.update("delete from products where id=?", product);
            jdbc.update("delete from customers where id=?", customer);
        }
    }
}
