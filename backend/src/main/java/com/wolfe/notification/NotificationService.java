package com.wolfe.notification;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final CustomerNotificationRepository repo;
    public NotificationService(CustomerNotificationRepository repo) {
        this.repo = repo;
    }
    public CustomerNotification orderStatus(Long customerId, String orderId, String status) {
        return repo.save(new CustomerNotification(customerId, "ORDER_STATUS", "Order "+orderId+" updated", "Your order is now "+status.replace('_',
        ' ').toLowerCase()+".", "ORDER",
        orderId));
    }
    public CustomerNotification returnUpdate(Long customerId, String orderId, String status) {
        return repo.save(new CustomerNotification(customerId, "RETURN_UPDATE", "Return request updated", "Your return request is now "+status.replace('_',
        ' ').toLowerCase()+".", "ORDER",
        orderId));
    }
    public CustomerNotification refundUpdate(Long customerId, String orderId, long amount) {
        return repo.save(new CustomerNotification(customerId, "REFUND_UPDATE", "Refund processed",
                "Your refund of ₹" + String.format(java.util.Locale.ROOT, "%.2f", amount / 100.0) + " has been processed for order " + orderId + ".", "ORDER", orderId));
    }
    public CustomerNotification cartRecovery(Long customerId) {
        return repo.save(new CustomerNotification(customerId, "CART_RECOVERY", "Your Wolfe selection is waiting",
        "Your saved cart is still waiting for you. Revisit your selection when you are ready.", "CART",
        customerId.toString()));
    }
    public CustomerNotification stockAlert(Long customerId, String slug, String name, int available) {
        return repo.save(new CustomerNotification(customerId, "STOCK_ALERT", name+" is back in stock", "Only "+available+" item(s) are currently available.",
        "PRODUCT",
        slug));
    }
}
