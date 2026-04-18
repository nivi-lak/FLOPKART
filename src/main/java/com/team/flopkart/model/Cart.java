// src/main/java/com/team/flopkart/model/Cart.java
package com.team.flopkart.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * JPA Entity representing a shopping cart.
 * One cart belongs to one User. A cart holds multiple CartItems.
 *
 * Design Principle (DIP): CartController never touches this class directly —
 * it always goes through ICartService.
 */

@Entity
@Table(name = "carts")
@Getter @Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each cart belongs to exactly one User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    //cascade = ALL (If cart is saved/deleted → items also saved/deleted)
    //orphanRemoval = true (If you remove item from list → deleted from DB)
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    //Tracks when cart was last modified
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
    public BigDecimal getSubtotal() {
    return items.stream()
            .map(item -> item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}

// Cart.java
// Represents entire cart of a user
// Stores items and calculates subtotal

// OOAD concept: Information Expert (GRASP)