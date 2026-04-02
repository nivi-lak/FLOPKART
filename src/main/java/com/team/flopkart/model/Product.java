package com.team.flopkart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Major class for Member 1: Product Management.
 * PLACEHOLDER: This is a placeholder implementation for testing purposes.
 * Full implementation will be provided by Member 1.
 */
@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String brand;
    private String imageUrl;
    private double price;
    private int discountPercent;
    private int stockQuantity;
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category; // PLACEHOLDER: FK to Category

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller; // PLACEHOLDER: FK to User (seller)

    private LocalDateTime createdAt;

    // Constructor for testing
    public Product(String name, double price, int stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.createdAt = LocalDateTime.now();
    }
}