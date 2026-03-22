// src/main/java/com/team/flopkart/repository/CartRepository.java
package com.team.flopkart.repository;

import com.team.flopkart.model.Cart;
import com.team.flopkart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for Cart entity.
 * Spring auto-implements all methods at runtime — no SQL needed.
 *
 * JpaRepository<Cart, Long> means:
 *   - Cart  → the entity this repo manages
 *   - Long  → the type of Cart's @Id field
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find the cart belonging to a specific user.
     * Used every time we need to load "the current user's cart".
     * Spring translates this to: SELECT * FROM carts WHERE user_id = ?
     */
    Optional<Cart> findByUser(User user);
}

// CartRepository.java
// Fetches cart from DB using user

// OOAD concept: Repository Pattern