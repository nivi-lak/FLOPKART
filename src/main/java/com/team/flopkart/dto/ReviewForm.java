package com.team.flopkart.dto;

import jakarta.validation.constraints.*;

/**
 * DTO for review form data.
 * Contains review information for product rating.
 */
public class ReviewForm {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment must be less than 1000 characters")
    private String comment;

    // Constructors
    public ReviewForm() {}

    public ReviewForm(Long productId, Long orderId, Integer rating, String comment) {
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
