package com.team.flopkart.repository;

import com.team.flopkart.model.Review;
import com.team.flopkart.model.Product;
import com.team.flopkart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Review entities.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find reviews by product.
     */
    List<Review> findByProduct(Product product);

    /**
     * Find reviews by user.
     */
    List<Review> findByUser(User user);

    /**
     * Check if user has already reviewed a product in a specific order.
     */
    boolean existsByProductAndUserAndOrderId(Product product, User user, Long orderId);
}
