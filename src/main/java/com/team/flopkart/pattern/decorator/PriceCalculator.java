package com.team.flopkart.pattern.decorator;

import java.math.BigDecimal;
 
//main contract
//every class in decorator pattern uses this
public interface PriceCalculator {
    
    // calc final price
    BigDecimal calculatePrice();
    
    //desc of price calc
    String getDescription();
}
 
