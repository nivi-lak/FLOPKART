package com.team.flopkart.pattern.observer;

import com.team.flopkart.model.Order;
import com.team.flopkart.model.OrderStatus;
import org.springframework.stereotype.Component;

/**
 * Observer that sends notifications on order status changes.
 * Demonstrates LSP: can be substituted for any OrderObserver.
 */
@Component
public class NotificationObserver implements OrderObserver {

    @Override
    public void onOrderStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // Simulate sending notification (e.g., email, SMS)
        System.out.println("Notification: Order " + order.getOrderNumber() + " status changed from " + oldStatus + " to " + newStatus);
        // In real app, integrate with email service
    }
}
