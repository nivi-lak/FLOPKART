package com.team.flopkart.service;

import com.team.flopkart.model.*;
import com.team.flopkart.repository.ReviewRepository;
import com.team.flopkart.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Review operations.
 * Demonstrates LSP: handles review logic without breaking substitutability.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }

    /**
     * Creates a new review for a product.
     * User can only review products from delivered orders.
     */
    public Review createReview(User user, Long productId, Long orderId, Integer rating, String comment) {
        // Validate that the user has a delivered order containing this product
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !order.getUser().equals(user) || order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Can only review products from delivered orders");
        }

        // Check if product is in the order
        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
        if (!productInOrder) {
            throw new IllegalArgumentException("Product not found in the specified order");
        }

        // Check if user already reviewed this product in this order
        Product product = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().get().getProduct();

        if (reviewRepository.existsByProductAndUserAndOrderId(product, user, orderId)) {
            throw new IllegalArgumentException("You have already reviewed this product from this order");
        }

        Review review = new Review(product, user, order, rating, comment);
        return reviewRepository.save(review);
    }

    /**
     * Gets all reviews for a product.
     */
    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    /**
     * Gets all reviews by a user.
     */
    public List<Review> getReviewsByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    /**
     * Calculates average rating for a product.
     */
    public double getAverageRating(Product product) {
        List<Review> reviews = getReviewsByProduct(product);
        if (reviews.isEmpty()) return 0.0;

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}
