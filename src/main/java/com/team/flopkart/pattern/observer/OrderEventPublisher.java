package com.team.flopkart.pattern.observer;

import com.team.flopkart.model.Order;
import com.team.flopkart.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Publisher for Order events using Observer pattern.
 * Behavioral pattern: notifies observers when order status changes.
 */
@Component
public class OrderEventPublisher {

    private final List<OrderObserver> observers = new ArrayList<>();

    public void addObserver(OrderObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(OrderObserver observer) {
        observers.remove(observer);
    }

    public void publishOrderStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        for (OrderObserver observer : observers) {
            observer.onOrderStatusChange(order, oldStatus, newStatus);
        }
    }
}
