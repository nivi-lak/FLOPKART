package com.team.flopkart.repository;

import com.team.flopkart.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.Optional;
 
/**
 * MEMBER 1's Repository - Used by Member 2's search functionality
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
}
 
