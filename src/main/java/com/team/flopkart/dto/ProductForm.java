package com.team.flopkart.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for product creation and editing form
 * Used by sellers to add/update products
 */
@Data
public class ProductForm {
    
    private Long id; // For editing existing products
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String name;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "9999999.99", message = "Price is too high")
    private BigDecimal price;
    
    @Min(value = 0, message = "Discount cannot be negative")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    private Integer discountPercent = 0;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Max(value = 999999, message = "Stock quantity too high")
    private Integer stockQuantity = 0;
    
    @Size(max = 500, message = "Image URL too long")
    private String imageUrl;
    
    @Size(max = 100, message = "Brand name too long")
    private String brand;
    
    private Boolean active = true;
    
    @NotNull(message = "Please select a category")
    private Long categoryId;
    
    // Helper method to check if this is a new product or edit
    public boolean isNew() {
        return id == null;
    }
}