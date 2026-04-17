package com.team.flopkart.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
/**
 * MEMBER 1 - MINOR CLASS
 * Category entity for product classification
 * (Included here for Member 2's ProductSearch filtering)
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 255)
    private String iconUrl;
}
 

