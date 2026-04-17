package com.team.flopkart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
 
/**
 * MEMBER 1 - MAJOR CLASS
 * Product entity managed by sellers
 * (Included here for Member 2's ProductSearch functionality)
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200)
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Min(value = 0, message = "Discount cannot be negative")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    @Column(nullable = false)
    private Integer discountPercent = 0;
    
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stockQuantity = 0;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Column(length = 100)
    private String brand;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
        if (discountPercent == null) discountPercent = 0;
        if (stockQuantity == null) stockQuantity = 0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate final price after discount
     */
    public BigDecimal getFinalPrice() {
        if (discountPercent == null || discountPercent == 0) {
            return price;
        }
        BigDecimal discount = price.multiply(BigDecimal.valueOf(discountPercent))
                                   .divide(BigDecimal.valueOf(100));
        return price.subtract(discount);
    }
    
    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
}
 
