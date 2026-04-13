package com.team.flopkart.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for checkout form data.
 * Contains shipping information for order placement.
 */
public class CheckoutForm {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Shipping city is required")
    private String shippingCity;

    @NotBlank(message = "Shipping pincode is required")
    private String shippingPincode;

    // Constructors
    public CheckoutForm() {}

    public CheckoutForm(String shippingAddress, String shippingCity, String shippingPincode) {
        this.shippingAddress = shippingAddress;
        this.shippingCity = shippingCity;
        this.shippingPincode = shippingPincode;
    }

    // Getters and Setters
    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingPincode() {
        return shippingPincode;
    }

    public void setShippingPincode(String shippingPincode) {
        this.shippingPincode = shippingPincode;
    }
}
