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
import java.util.stream.Collectors;
import java.util.Optional;

import com.team.flopkart.pattern.decorator.BasePriceCalculator;
import com.team.flopkart.pattern.decorator.DiscountDecorator;
import com.team.flopkart.pattern.decorator.TaxDecorator;
import com.team.flopkart.pattern.decorator.PriceCalculator;


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

        // Calculate total amount WITH decorator chain (discount + GST)
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            
            // DECORATOR CHAIN — built per product at runtime
            PriceCalculator calculator = new BasePriceCalculator(cartItem.getProduct());
            
            if (cartItem.getProduct().getDiscountPercent() != null 
                    && cartItem.getProduct().getDiscountPercent() > 0) {
                calculator = new DiscountDecorator(calculator, cartItem.getProduct().getDiscountPercent());
            }
            
            // Always apply 18% GST
            calculator = new TaxDecorator(calculator, 18.0);
            
            BigDecimal priceWithTax = calculator.calculatePrice();
            
            totalAmount = totalAmount.add(
                priceWithTax.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        // Create order
        Order order = new Order(user, null, totalAmount, shippingAddress, shippingCity, shippingPincode);
        order = orderRepository.save(order);

        // Create order items with tax-inclusive price
        List<OrderItem> savedItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            
            // Run chain again per item to get individual final price
            PriceCalculator calculator = new BasePriceCalculator(cartItem.getProduct());
            
            if (cartItem.getProduct().getDiscountPercent() != null 
                    && cartItem.getProduct().getDiscountPercent() > 0) {
                calculator = new DiscountDecorator(calculator, cartItem.getProduct().getDiscountPercent());
            }
            
            calculator = new TaxDecorator(calculator, 18.0);
            
            BigDecimal finalPrice = calculator.calculatePrice(); // ← tax inclusive
            
            OrderItem orderItem = new OrderItem(
                order,
                cartItem.getProduct(),
                cartItem.getQuantity(),
                finalPrice  // ← was cartItem.getProduct().getPrice()
            );
            orderItem = orderItemRepository.save(orderItem);
            savedItems.add(orderItem);
        }
        order.setItems(savedItems);
        orderRepository.save(order);

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
        // Get all orders
        List<Order> allOrders = orderRepository.findAll();
        
        // Filter orders that contain at least one product from this seller
        return allOrders.stream()
            .filter(order -> order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSeller().getId().equals(seller.getId())))
            .collect(Collectors.toList());
    }
    public List<Order> getOrdersBySellerAndStatus(Seller seller, OrderStatus status) {
        return getOrdersBySeller(seller).stream()
            .filter(order -> order.getStatus() == status)
            .collect(Collectors.toList());
    }
    public Optional<Order> getOrderByIdOptional(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    /**
     * Update order status
     */
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    /**
     * 
     * Finds order by order number.
     */
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber).orElse(null);
    }
}
