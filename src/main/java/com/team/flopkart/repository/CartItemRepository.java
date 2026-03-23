// src/main/java/com/team/flopkart/repository/CartItemRepository.java
package com.team.flopkart.repository;

import com.team.flopkart.model.Cart;
import com.team.flopkart.model.CartItem;
import com.team.flopkart.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for CartItem entity.
 */
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find a specific item in a cart by product.
     * Used when adding to cart — if item already exists, we update
     * quantity instead of creating a duplicate row.
     * SQL: SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
}

// CartItemRepository.java
// Finds item in cart by product (avoid duplicates)

// OOAD concept: Repository Pattern + Query Abstraction