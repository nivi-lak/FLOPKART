package com.team.flopkart.pattern.decorator;

import java.math.BigDecimal;
 
/**
 * MEMBER 2 - DECORATOR PATTERN
 * 
 * Abstract decorator base class.
 * All concrete decorators will extend this class.
 */
public abstract class PriceCalculatorDecorator implements PriceCalculator {
    
    protected PriceCalculator wrappedCalculator;
    
    public PriceCalculatorDecorator(PriceCalculator calculator) {
        this.wrappedCalculator = calculator;
    }
    
    @Override
    public BigDecimal calculatePrice() {
        return wrappedCalculator.calculatePrice();
    }
    
    @Override
    public String getDescription() {
        return wrappedCalculator.getDescription();
    }
}
 
