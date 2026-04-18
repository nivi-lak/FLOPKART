package com.team.flopkart.pattern.decorator;
import com.team.flopkart.model.Product;

import java.math.BigDecimal;
 
/**
 * MEMBER 2 - DECORATOR PATTERN
 * 
 * Base implementation that returns the product's base price.
 * This is the core component that decorators will wrap.
 */
public class BasePriceCalculator implements PriceCalculator {
    
    private final Product product;
    
    public BasePriceCalculator(Product product) {
        this.product = product;
    }
    
    @Override
    public BigDecimal calculatePrice() {
        return product.getPrice();
    }
    
    @Override
    public String getDescription() {
        return String.format("Base Price: ₹%.2f", product.getPrice());
    }
    
    public Product getProduct() {
        return product;
    }
}

