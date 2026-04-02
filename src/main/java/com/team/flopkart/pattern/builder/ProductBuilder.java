package com.team.flopkart.pattern.builder;

import com.team.flopkart.model.Product;
import com.team.flopkart.model.Category;
import com.team.flopkart.model.User;

/**
 * Builder pattern for Product construction.
 * Creational pattern: constructs Product objects step by step without telescoping constructors.
 * Demonstrates OCP: adding new fields doesn't require modifying existing code.
 * PLACEHOLDER: This is a placeholder implementation for testing purposes.
 * Full implementation will be provided by Member 1.
 */
public class ProductBuilder {

    private String name;
    private String description;
    private double price;
    private int discountPercent;
    private int stockQuantity;
    private String brand;
    private String imageUrl;
    private Category category;
    private User seller;
    private boolean active = true;

    public ProductBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ProductBuilder price(double price) {
        this.price = price;
        return this;
    }

    public ProductBuilder discount(int discountPercent) {
        this.discountPercent = discountPercent;
        return this;
    }

    public ProductBuilder stock(int stockQuantity) {
        this.stockQuantity = stockQuantity;
        return this;
    }

    public ProductBuilder brand(String brand) {
        this.brand = brand;
        return this;
    }

    public ProductBuilder imageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public ProductBuilder category(Category category) {
        this.category = category;
        return this;
    }

    public ProductBuilder seller(User seller) {
        this.seller = seller;
        return this;
    }

    public ProductBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    public Product build() {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setDiscountPercent(discountPercent);
        product.setStockQuantity(stockQuantity);
        product.setBrand(brand);
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setSeller(seller);
        product.setActive(active);
        return product;
    }
}
