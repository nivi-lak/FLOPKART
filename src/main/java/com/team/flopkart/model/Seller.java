package com.team.flopkart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//This class is responsible ONLY for seller profile data

@Entity
@Table(name = "sellers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seller {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    //Link to the User account with SELLER role
    // One User can have one Seller profile
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
    
    public void verify() {
        this.isVerified = true;
    }
    
    public void unverify() {
        this.isVerified = false;
    }
}
