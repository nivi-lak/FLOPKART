package com.team.flopkart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import java.time.LocalDateTime;
 
/**
 * SHARED INFRASTRUCTURE - Used by all members
 * User entity for authentication and authorization
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password; // BCrypt encoded
    
    @Column(length = 10)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role = UserRole.CUSTOMER;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (enabled == null) {
            enabled = true;
        }
        if (role == null) {
            role = UserRole.CUSTOMER;
        }
    }
}
