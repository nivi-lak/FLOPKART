package com.team.flopkart.pattern.decorator;
import java.math.BigDecimal;
import java.math.RoundingMode;
 
/**
 * MEMBER 2 - DECORATOR PATTERN
 * 
 * Decorator that applies a coupon code discount.
 * Coupon is typically applied after product discount but before/after tax.
 */
public class CouponDecorator extends PriceCalculatorDecorator {
    
    private final String couponCode;
    private final Double couponDiscountPercent;
    
    /**
     * @param calculator Previous calculator in the chain
     * @param couponCode Coupon code being applied
     * @param couponDiscountPercent Discount percentage from coupon (e.g., 20.0 for 20%)
     */
    public CouponDecorator(PriceCalculator calculator, 
                           String couponCode, 
                           Double couponDiscountPercent) {
        super(calculator);
        this.couponCode = couponCode;
        this.couponDiscountPercent = couponDiscountPercent != null ? couponDiscountPercent : 0.0;
    }
    
    @Override
    public BigDecimal calculatePrice() {
        BigDecimal priceBeforeCoupon = wrappedCalculator.calculatePrice();
        
        if (couponDiscountPercent == 0.0 || couponCode == null || couponCode.isEmpty()) {
            return priceBeforeCoupon;
        }
        
        // Calculate coupon discount amount
        BigDecimal couponDiscount = priceBeforeCoupon
            .multiply(BigDecimal.valueOf(couponDiscountPercent))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Subtract coupon discount
        return priceBeforeCoupon.subtract(couponDiscount);
    }
    
    @Override
    public String getDescription() {
        if (couponDiscountPercent == 0.0 || couponCode == null || couponCode.isEmpty()) {
            return wrappedCalculator.getDescription();
        }
        
        BigDecimal priceBeforeCoupon = wrappedCalculator.calculatePrice();
        BigDecimal couponSavings = priceBeforeCoupon.subtract(calculatePrice());
        
        return wrappedCalculator.getDescription() + 
               String.format(" → Coupon '%s' (%.1f%%, -₹%.2f): ₹%.2f", 
                           couponCode,
                           couponDiscountPercent,
                           couponSavings,
                           calculatePrice());
    }
}
