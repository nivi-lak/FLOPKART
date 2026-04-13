package com.team.flopkart.model;

/**
 * Enum representing the possible statuses of an Order.
 * Demonstrates LSP (Liskov Substitution Principle) as subtypes of OrderObserver
 * can be substituted without breaking the system.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
