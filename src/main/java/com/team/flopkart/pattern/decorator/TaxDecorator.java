package com.team.flopkart.pattern.decorator;
import java.math.BigDecimal;
import java.math.RoundingMode;
 
//applies tax
//after discount
public class TaxDecorator extends PriceCalculatorDecorator {
    
    private final Double taxPercent;
    
 
    public TaxDecorator(PriceCalculator calculator, Double taxPercent) {
        super(calculator);
        this.taxPercent = taxPercent != null ? taxPercent : 0.0;
    }
    
    @Override
    public BigDecimal calculatePrice() {
        BigDecimal priceAfterPreviousCalculations = wrappedCalculator.calculatePrice();
        
        if (taxPercent == 0.0) {
            return priceAfterPreviousCalculations;
        }
        
        // Calculate tax amount
        BigDecimal taxAmount = priceAfterPreviousCalculations
            .multiply(BigDecimal.valueOf(taxPercent))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        return priceAfterPreviousCalculations.add(taxAmount);
    }
    
    @Override
    public String getDescription() {
        BigDecimal priceBeforeTax = wrappedCalculator.calculatePrice();
        BigDecimal priceAfterTax = calculatePrice();
        BigDecimal taxAmount = priceAfterTax.subtract(priceBeforeTax);
        
        return wrappedCalculator.getDescription() + 
               String.format(" → Tax (%.1f%%, ₹%.2f): ₹%.2f", 
                           taxPercent,
                           taxAmount,
                           priceAfterTax);
    }
}
