package com.team.flopkart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Shop name is required")
    private String shopName;

    @Pattern(regexp = "^[0-9]{15}$", message = "GST number must be 15 digits")
    private String gstNumber;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    private String bankAccount;
}