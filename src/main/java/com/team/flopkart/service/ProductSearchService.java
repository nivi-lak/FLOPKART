package com.team.flopkart.service;
import com.team.flopkart.model.Category;
import com.team.flopkart.model.Product;
import com.team.flopkart.repository.CategoryRepository;
import com.team.flopkart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.math.BigDecimal;
import java.util.List;
 
/**
 * MEMBER 2 - MINOR USE CASE SERVICE (ProductSearch)
 * 
 * Service for product browsing, searching, filtering, and sorting.
 * This is NOT an entity - it's a service class that orchestrates product discovery.
 * 
 * DEMONSTRATES SRP:
 * - This service handles ONLY product search and filtering logic
 * - Product CRUD is NOT here (that's ProductService - Member 1)
 * - Seller management is NOT here (that's SellerService - Member 2)
 * 
 * Features:
 * - Keyword search (name, description, brand)
 * - Filter by category
 * - Filter by price range
 * - Sort by price (low to high, high to low)
 * - Pagination
 * - Combined filters
 */
@Service
@Transactional(readOnly = true)
public class ProductSearchService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    // Default values for search
    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final BigDecimal DEFAULT_MIN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_MAX_PRICE = new BigDecimal("999999.99");
    
    public ProductSearchService(ProductRepository productRepository,
                                CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    
    /**
     * Browse all active products with pagination
     */
    public Page<Product> browseProducts(int page, String sortBy) {
        Pageable pageable = createPageable(page, sortBy);
        return productRepository.findByActiveTrue(pageable);
    }
    
    /**
     * Search products by keyword
     */
    public Page<Product> searchByKeyword(String keyword, int page, String sortBy) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return browseProducts(page, sortBy);
        }
        
        Pageable pageable = createPageable(page, sortBy);
        return productRepository.searchByKeyword(keyword.trim(), pageable);
    }
    
    /**
     * Filter products by category
     */
    public Page<Product> filterByCategory(Long categoryId, int page, String sortBy) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        Pageable pageable = createPageable(page, sortBy);
        return productRepository.findByActiveTrueAndCategory(category, pageable);
    }
    
    /**
     * Filter products by price range
     */
    public Page<Product> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, 
                                             int page, String sortBy) {
        BigDecimal min = minPrice != null ? minPrice : DEFAULT_MIN_PRICE;
        BigDecimal max = maxPrice != null ? maxPrice : DEFAULT_MAX_PRICE;
        
        Pageable pageable = createPageable(page, sortBy);
        return productRepository.findByPriceRange(min, max, pageable);
    }
    
    /**
     * Search with keyword and category filter combined
     */
    public Page<Product> searchWithCategory(String keyword, Long categoryId, 
                                              int page, String sortBy) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return filterByCategory(categoryId, page, sortBy);
        }
        
        Pageable pageable = createPageable(page, sortBy);
        return productRepository.searchByKeywordAndCategory(keyword.trim(), category, pageable);
    }
    
    /**
     * Advanced search with all filters combined
     * 
     * @param keyword Search keyword (can be null/empty)
     * @param categoryId Category filter (can be null)
     * @param minPrice Minimum price (can be null)
     * @param maxPrice Maximum price (can be null)
     * @param page Page number (0-indexed)
     * @param sortBy Sort criteria
     * @return Page of filtered products
     */
    public Page<Product> advancedSearch(String keyword, Long categoryId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         int page, String sortBy) {
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) 
                               ? keyword.trim() : "";
        BigDecimal min = minPrice != null ? minPrice : DEFAULT_MIN_PRICE;
        BigDecimal max = maxPrice != null ? maxPrice : DEFAULT_MAX_PRICE;
        
        Pageable pageable = createPageable(page, sortBy);
        return productRepository.searchWithAllFilters(searchKeyword, categoryId, min, max, pageable);
    }
    
    /**
     * Get featured products (on sale, high discount)
     */
    public List<Product> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findFeaturedProducts(pageable);
    }
    
    /**
     * Filter by brand
     */
    public Page<Product> filterByBrand(String brand, int page, String sortBy) {
        Pageable pageable = createPageable(page, sortBy);
        return productRepository.findByActiveTrueAndBrandContainingIgnoreCase(brand, pageable);
    }
    
    /**
     * Get all categories for filter dropdown
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    /**
     * Create pageable object with sorting
     * 
     * Sort options:
     * - "price-asc": Price low to high
     * - "price-desc": Price high to low
     * - "name": Alphabetical by name
     * - "newest": Recently added first
     */
    private Pageable createPageable(int page, String sortBy) {
        Sort sort;
        
        switch (sortBy != null ? sortBy : "newest") {
            case "price-asc":
                sort = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "price-desc":
                sort = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "name":
                sort = Sort.by(Sort.Direction.ASC, "name");
                break;
            case "newest":
            default:
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
        }
        
        return PageRequest.of(page, DEFAULT_PAGE_SIZE, sort);
    }
    
    /**
     * Count total active products
     */
    public long getTotalActiveProducts() {
        return productRepository.findByActiveTrue().size();
    }
}
