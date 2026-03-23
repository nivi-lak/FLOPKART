package com.team.flopkart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Seller entity — Member 2 (Major Class).
 *
 * Design Principle: SRP (Single Responsibility Principle)
 * This class holds ONLY seller profile data.
 * It does NOT handle product CRUD (that is Product's responsibility)
 * and does NOT handle search (that is ProductSearchService's responsibility).
 */
@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one link to the authenticated User.
     * A Seller is always backed by a User account with role SELLER.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotBlank
    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Column(name = "gst_number", unique = true)
    private String gstNumber;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    private void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}