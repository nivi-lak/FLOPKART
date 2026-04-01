package com.team.flopkart.pattern.observer;

import com.team.flopkart.model.Order;
import com.team.flopkart.model.OrderStatus;

/**
 * Observer interface for Order status changes.
 * Demonstrates LSP: any subtype can be substituted without breaking the system.
 */
public interface OrderObserver {

    void onOrderStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus);
}
