package com.team.flopkart.pattern.decorator;

import java.math.BigDecimal;
 
/**
 * MEMBER 2 - DESIGN PATTERN: DECORATOR (Structural)
 * 
 * Base interface for price calculation.
 * 
 * WHY DECORATOR PATTERN:
 * - Allows dynamic addition of pricing rules (discount, tax, coupon) 
 *   without modifying the base calculator
 * - Each decorator wraps the previous one, creating a chain of calculations
 * - New pricing rules can be added without changing existing code (OCP compliant)
 * - Provides flexibility to apply different combinations of pricing rules
 * 
 * EXAMPLE USAGE:
 * PriceCalculator calculator = new CouponDecorator(
 *     new TaxDecorator(
 *         new DiscountDecorator(
 *             new BasePriceCalculator(product)
 *         ), 18.0
 *     ), "SAVE20", 20.0
 * );
 * BigDecimal finalPrice = calculator.calculatePrice();
 */
public interface PriceCalculator {
    
    /**
     * Calculate the final price with all applicable rules
     * @return Final calculated price
     */
    BigDecimal calculatePrice();
    
    /**
     * Get description of the price calculation
     * @return Human-readable breakdown
     */
    String getDescription();
}
 
