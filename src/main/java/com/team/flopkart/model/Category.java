package com.team.flopkart.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * Minor class for Member 1: Product Management.
 * PLACEHOLDER: This is a placeholder implementation for testing purposes.
 * Full implementation will be provided by Member 1.
 */
@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String iconUrl;

    // Constructor for testing
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
