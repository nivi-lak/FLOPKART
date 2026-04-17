package com.team.flopkart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * MEMBER 2 - MAJOR CLASS
 * 
 * Seller entity representing a vendor/merchant in the marketplace.
 * 
 * DESIGN PRINCIPLE APPLIED: SRP (Single Responsibility Principle)
 * - This class is responsible ONLY for seller profile data
 * - Product CRUD is handled by Product class (Member 1)
 * - Product search/filtering is handled by ProductSearch service (Member 2)
 * - Each class has exactly one reason to change
 * 
 * Demonstrates SRP by:
 * 1. Seller contains only seller-specific attributes (shop details, verification status)
 * 2. Does NOT contain product management logic
 * 3. Does NOT contain search/filter logic
 * 4. Separation of concerns: profile data vs. business operations
 */
@Entity
@Table(name = "sellers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seller {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Link to the User account with SELLER role
     * One User can have one Seller profile
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @NotBlank(message = "Shop name is required")
    @Column(nullable = false, length = 100)
    private String shopName;
    
    @NotBlank(message = "GST number is required")
    @Column(nullable = false, length = 15)
    private String gstNumber;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", 
             message = "Invalid phone number")
    @Column(nullable = false, length = 10)
    private String phoneNumber;
    
    @NotBlank(message = "Bank account number is required")
    @Column(nullable = false, length = 50)
    private String bankAccount;
    
    @Column(nullable = false)
    private Boolean isVerified = false;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;
    
    @Column(length = 500)
    private String shopDescription;
    
    @Column(length = 255)
    private String shopLogoUrl;
    
    @Column(length = 200)
    private String businessAddress;
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        if (isVerified == null) {
            isVerified = false;
        }
    }
    
    /**
     * Business method demonstrating SRP:
     * Only handles verification status toggle - no product logic
     */
    public void verify() {
        this.isVerified = true;
    }
    
    public void unverify() {
        this.isVerified = false;
    }
}
