package com.team.flopkart.pattern.decorator;
import com.team.flopkart.model.Product;

import java.math.BigDecimal;
 

//Base implementation - returns the product's base price

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

