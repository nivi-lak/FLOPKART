package com.team.flopkart.pattern.decorator;

import java.math.BigDecimal;
import java.math.RoundingMode;
 
//applies discount
public class DiscountDecorator extends PriceCalculatorDecorator {
    
    private final Integer discountPercent;
    
    public DiscountDecorator(PriceCalculator calculator, Integer discountPercent) {
        super(calculator);
        this.discountPercent = discountPercent != null ? discountPercent : 0;
    }
    
    @Override
    public BigDecimal calculatePrice() {
        BigDecimal basePrice = wrappedCalculator.calculatePrice();
        
        if (discountPercent == 0) {
            return basePrice;
        }
        
        // Calculate discount amount
        BigDecimal discountAmount = basePrice
            .multiply(BigDecimal.valueOf(discountPercent))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        return basePrice.subtract(discountAmount);
    }
    
    @Override
    public String getDescription() {
        return wrappedCalculator.getDescription() + 
               String.format(" → Discount (%d%%): ₹%.2f", 
                           discountPercent, 
                           calculatePrice());
    }
}
