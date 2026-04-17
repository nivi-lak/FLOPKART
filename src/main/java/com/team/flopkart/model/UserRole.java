package com.team.flopkart.model;

/**
 * Enum representing the roles a user can hold in Flopkart.
 * Used by Spring Security for role-based access control.
 */
public enum UserRole {
    CUSTOMER,
    SELLER,
    ADMIN
}
