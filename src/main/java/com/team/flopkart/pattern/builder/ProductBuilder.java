package com.team.flopkart.pattern.builder;

import com.team.flopkart.model.Product;

import java.math.BigDecimal;

import com.team.flopkart.model.Category;
import com.team.flopkart.model.Seller;

public class ProductBuilder {

    private String name;
    private String description;
    private BigDecimal price;
    private int discountPercent;
    private int stockQuantity;
    private String brand;
    private String imageUrl;
    private Category category;
    private Seller seller;
    private boolean active = true;

    public ProductBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ProductBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ProductBuilder price(BigDecimal price) {
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

    public ProductBuilder seller(Seller seller) {
        this.seller = seller;
        return this;
    }

    public ProductBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    public Product build() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Product name is required");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Valid price is required");
        }

        if (seller == null) {
            throw new IllegalStateException("Seller is required");
        }
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
