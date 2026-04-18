package com.team.flopkart.repository;

import com.team.flopkart.model.Category;
import com.team.flopkart.model.Product;
import com.team.flopkart.model.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.math.BigDecimal;
import java.util.List;
 
/**
 * MEMBER 1's Repository - Used by Member 2's ProductSearch service
 * Repository for Product entity with search capabilities
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Basic queries
    List<Product> findByActiveTrue();
    
    List<Product> findBySeller(Seller seller);
    
    Page<Product> findByActiveTrue(Pageable pageable);
    
    // Search by keyword
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Filter by category
    Page<Product> findByActiveTrueAndCategory(Category category, Pageable pageable);
    
    // Filter by price range
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                     @Param("maxPrice") BigDecimal maxPrice, 
                                     Pageable pageable);
    
    // Combined search with keyword and category
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "p.category = :category AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByKeywordAndCategory(@Param("keyword") String keyword,
                                               @Param("category") Category category,
                                               Pageable pageable);
    
    // Combined search with all filters
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "p.price BETWEEN :minPrice AND :maxPrice")
    Page<Product> searchWithAllFilters(@Param("keyword") String keyword,
                                         @Param("categoryId") Long categoryId,
                                         @Param("minPrice") BigDecimal minPrice,
                                         @Param("maxPrice") BigDecimal maxPrice,
                                         Pageable pageable);
    
    // Get featured products (high stock, active, with discount)
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "p.stockQuantity > 0 AND p.discountPercent > 0 " +
           "ORDER BY p.discountPercent DESC")
    List<Product> findFeaturedProducts(Pageable pageable);
    
    // Find products by brand
    Page<Product> findByActiveTrueAndBrandContainingIgnoreCase(String brand, Pageable pageable);
}
