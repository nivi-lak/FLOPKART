// src/main/java/com/team/flopkart/service/ICartService.java
package com.team.flopkart.service;

import com.team.flopkart.model.Cart;
import com.team.flopkart.model.User;

/**
 * Interface defining the Cart service contract.
 *
 * Design Principle — DIP (Dependency Inversion Principle):
 * CartController depends on THIS interface, not on CartService directly.
 * High-level modules (Controller) must not depend on low-level modules (Service impl).
 * Both depend on this abstraction.
 */
public interface ICartService {

    /** Get or create the cart for a user */
    Cart getOrCreateCart(User user);

    /** Add a product to the cart (or increment qty if already present) */
    Cart addToCart(User user, Long productId, int quantity);

    /** Update the quantity of an existing cart item */
    Cart updateQuantity(User user, Long cartItemId, int newQuantity);

    /** Remove a single item from the cart */
    Cart removeItem(User user, Long cartItemId);

    /** Empty the entire cart (used after order is placed) */
    void clearCart(User user);
}

// ICartService.java
// Defines what cart operations exist (contract)

// OOAD concept: DIP (Dependency Inversion Principle)