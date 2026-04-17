package com.team.flopkart.service;

import com.team.flopkart.model.*;
import com.team.flopkart.repository.OrderRepository;
import com.team.flopkart.repository.OrderItemRepository;
import com.team.flopkart.pattern.observer.OrderEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Service for Order operations.
 * Demonstrates DIP: implements business logic for orders.
 * Uses Observer pattern for order status notifications.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    /**
     * Creates a new order from cart items.
     * Converts CartItems to OrderItems and calculates total.
     */
    @Transactional
    public Order createOrder(User user, List<CartItem> cartItems, String shippingAddress, String shippingCity, String shippingPincode) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart items cannot be empty");
        }

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            totalAmount = totalAmount.add(
                cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        // Create order
        Order order = new Order(user, null, totalAmount, shippingAddress, shippingCity, shippingPincode);
        order = orderRepository.save(order);

        // Create order items and populate order relationship
        List<OrderItem> savedItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem(
                order,
                cartItem.getProduct(),
                cartItem.getQuantity(),
                cartItem.getProduct().getPrice()
            );
            orderItem = orderItemRepository.save(orderItem);
            savedItems.add(orderItem);
        }
        order.setItems(savedItems);
        orderRepository.save(order);

        // Publish order creation event (status change from null to PENDING)
        orderEventPublisher.publishOrderStatusChange(order, null, OrderStatus.PENDING);

        return order;
    }

    /**
     * Updates order status and notifies observers.
     */
    @Transactional
    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);

        // Notify observers of status change
        orderEventPublisher.publishOrderStatusChange(order, oldStatus, newStatus);
    }

    /**
     * Finds order by ID.
     */
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    /**
     * Finds all orders for a user.
     */
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    /**
     * Finds all delivered orders for a user (for review eligibility).
     */
    public List<Order> getDeliveredOrdersByUser(User user) {
        return orderRepository.findByUser(user).stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .toList();
    }

    public List<Order> getOrdersBySeller(Seller seller) {
        List<OrderItem> orderItems = orderItemRepository.findByProductSeller(seller);

        List<Order> orders = new ArrayList<>();
        for (OrderItem item : orderItems) {
            if (!orders.contains(item.getOrder())) {
                orders.add(item.getOrder());
            }
        }

        return orders;
    }

    /**
     * Finds order by order number.
     */
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElse(null);
    }
}
