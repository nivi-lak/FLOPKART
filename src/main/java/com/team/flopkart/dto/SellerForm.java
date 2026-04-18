package com.team.flopkart.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * SellerForm DTO — Member 2.
 * Backs the seller registration HTML form.
 * Keeps validation logic out of the Seller entity (SRP).
 */
@Getter
@Setter
public class SellerForm {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Phone must be 10 digits starting with 6-9")
    private String phone;
    
    // Seller-specific details
    @NotBlank(message = "Shop name is required")
    @Size(min = 3, max = 100, message = "Shop name must be between 3 and 100 characters")
    private String shopName;
    
    @NotBlank(message = "GST number is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$", 
             message = "Invalid GST format (e.g., 29ABCDE1234F1Z5)")
    private String gstNumber;
    
    @NotBlank(message = "Bank account number is required")
    @Size(min = 9, max = 18, message = "Invalid bank account number")
    private String bankAccount;
    
    private String businessAddress;
    
    private String shopDescription;
}
