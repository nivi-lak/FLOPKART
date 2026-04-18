package com.team.flopkart.pattern.decorator;
import java.math.BigDecimal;
import java.math.RoundingMode;
 
/**
 * MEMBER 2 - DECORATOR PATTERN
 * 
 * Decorator that applies tax (GST) to the price.
 * Tax is applied AFTER discount (Indian GST standard).
 */
public class TaxDecorator extends PriceCalculatorDecorator {
    
    private final Double taxPercent;
    
    /**
     * @param calculator Previous calculator in the chain
     * @param taxPercent Tax percentage (e.g., 18.0 for 18% GST)
     */
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
        
        // Add tax to the price
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
