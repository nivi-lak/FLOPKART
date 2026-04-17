package com.team.flopkart.controller;

import com.team.flopkart.model.Category;
import com.team.flopkart.model.Product;
import com.team.flopkart.pattern.decorator.*;
import com.team.flopkart.service.ProductSearchService;
import com.team.flopkart.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * MEMBER 2 - MINOR USE CASE CONTROLLER (ProductSearch)
 * 
 * Controller for customer product browsing, search, and filtering.
 * 
 * USE CASE FLOWS:
 * 1. Browse Products:
 *    - Customer lands on product listing page
 *    - Sees all active products with pagination
 *    - Can sort by price/name/newest
 * 
 * 2. Search Products:
 *    - Customer enters keyword in search box
 *    - Results filtered by name/description/brand
 * 
 * 3. Filter Products:
 *    - Filter by category (dropdown)
 *    - Filter by price range (slider/inputs)
 *    - Combine multiple filters
 * 
 * 4. View Product Details:
 *    - Click on product to see full details
 *    - Shows price calculation using Decorator pattern
 */
@Controller
@RequestMapping("/products")
public class ProductBrowseController {
    
    private final ProductSearchService productSearchService;
    private final ProductService productService;
    public ProductBrowseController(ProductSearchService productSearchService , ProductService productService) {
        this.productSearchService = productSearchService;
        this.productService = productService;
    }
    
    /**
     * Main product listing page
     */
    @GetMapping
    public String browseProducts(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "newest") String sort,
                                  Model model) {
        
        Page<Product> productPage = productSearchService.browseProducts(page, sort);
        List<Category> categories = productSearchService.getAllCategories();
        
        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortBy", sort);
        
        return "products/browse";
    }
    
    /**
     * Search products by keyword
     */
    @GetMapping("/search")
    public String searchProducts(@RequestParam(required = false) String keyword,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "newest") String sort,
                                  Model model) {
        
        Page<Product> productPage = productSearchService.searchByKeyword(keyword, page, sort);
        List<Category> categories = productSearchService.getAllCategories();
        
        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortBy", sort);
        
        return "products/search-results";
    }
    
    /**
     * Advanced search with all filters
     */
    @GetMapping("/filter")
    public String filterProducts(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Long categoryId,
                                  @RequestParam(required = false) BigDecimal minPrice,
                                  @RequestParam(required = false) BigDecimal maxPrice,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "newest") String sort,
                                  Model model) {
        
        Page<Product> productPage = productSearchService.advancedSearch(
            keyword, categoryId, minPrice, maxPrice, page, sort
        );
        
        List<Category> categories = productSearchService.getAllCategories();
        
        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortBy", sort);
        
        return "products/search-results";
    }
    
    /**
     * Filter by category only
     */
    @GetMapping("/category/{categoryId}")
    public String filterByCategory(@PathVariable Long categoryId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "newest") String sort,
                                    Model model) {
        
        Page<Product> productPage = productSearchService.filterByCategory(categoryId, page, sort);
        List<Category> categories = productSearchService.getAllCategories();
        
        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sortBy", sort);
        
        return "products/search-results";
    }
    
    /**
     * View single product details
     * 
     * DEMONSTRATES DECORATOR PATTERN:
     * Shows price breakdown using decorator chain
     */
    @GetMapping("/{productId}")
    public String viewProductDetails(@PathVariable Long productId,
                                      @RequestParam(required = false) String couponCode,
                                      Model model) {
        
        // In a real app, this would come from ProductService (Member 1)
        // For demo purposes, we'll show how to use the decorator pattern
        Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
        System.out.println(product);
        model.addAttribute("product", product);

        // Optional (to avoid errors)
        model.addAttribute("reviews", new ArrayList<>());
        model.addAttribute("averageRating", 0);
        model.addAttribute("productId", productId);
        
        // Decorator pattern will be demonstrated in the template
        // by showing price calculation breakdown
        
        return "products/detail";
    }
    
    /**
     * API endpoint to calculate price with decorators
     * (AJAX endpoint for dynamic price calculation)
     */
    @GetMapping("/{productId}/calculate-price")
    @ResponseBody
    public PriceBreakdown calculatePrice(@PathVariable Long productId,
                                          @RequestParam(defaultValue = "false") boolean applyTax,
                                          @RequestParam(required = false) String couponCode,
                                          @RequestParam(required = false) Double couponDiscount) {
        
        // This is a simplified example showing the Decorator pattern in action
        // In real implementation, product would be fetched from database
        
        // For demonstration purposes only - showing pattern usage
        return new PriceBreakdown(
            "Product price calculation using Decorator pattern",
            "See DecoratorDemo.java for full working example"
        );
    }
    
    /**
     * Featured/promoted products page
     */
    @GetMapping("/featured")
    public String showFeaturedProducts(Model model) {
        List<Product> featuredProducts = productSearchService.getFeaturedProducts(12);
        List<Category> categories = productSearchService.getAllCategories();
        
        model.addAttribute("products", featuredProducts);
        model.addAttribute("categories", categories);
        
        return "products/featured";
    }
    
    // Inner class for API response
    static class PriceBreakdown {
        public String message;
        public String note;
        
        public PriceBreakdown(String message, String note) {
            this.message = message;
            this.note = note;
        }
    }
}
