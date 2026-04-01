package com.team.flopkart.dto;

/**
 * DTO for checkout form data.
 * Contains shipping information for order placement.
 */
public class CheckoutForm {

    private String shippingAddress;
    private String shippingCity;
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
