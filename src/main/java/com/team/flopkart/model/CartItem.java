// src/main/java/com/team/flopkart/model/CartItem.java
package com.team.flopkart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * JPA Entity representing a single line item inside a Cart.
 * Tracks which product and how many units.
 *
 * Minor use case: individual line item management within the cart.
 */

@Entity
@Table(name = "cart_items")
@Getter @Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many CartItems belong to one Cart
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Many CartItems can reference the same Product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

}

// CartItem.java
// Represents one product + quantity inside cart

// OOAD concept: Association (Cart ↔ Product)