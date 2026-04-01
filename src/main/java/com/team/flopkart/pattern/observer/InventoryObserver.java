package com.team.flopkart.pattern.observer;

import com.team.flopkart.model.Order;
import com.team.flopkart.model.OrderStatus;
import org.springframework.stereotype.Component;

/**
 * Observer that updates inventory on order status changes.
 * Demonstrates LSP: can be substituted for any OrderObserver.
 */
@Component
public class InventoryObserver implements OrderObserver {

    @Override
    public void onOrderStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING) {
            // Deduct inventory when order is confirmed
            order.getItems().forEach(item -> {
                int newStock = item.getProduct().getStockQuantity() - item.getQuantity();
                item.getProduct().setStockQuantity(Math.max(0, newStock));
                System.out.println("Inventory updated for product " + item.getProduct().getName() + ": " + newStock + " remaining");
            });
        }
        // In real app, save product changes
    }
}
