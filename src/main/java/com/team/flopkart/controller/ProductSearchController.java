package com.team.flopkart.controller;

import com.team.flopkart.model.Category;
import com.team.flopkart.model.Product;
import com.team.flopkart.model.Order;
import com.team.flopkart.pattern.decorator.*;
import com.team.flopkart.service.ProductSearchService;
import com.team.flopkart.service.ProductService;
import com.team.flopkart.service.ReviewService;
import com.team.flopkart.service.OrderService;
import com.team.flopkart.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// calculatePrice() endpoint builds the chain at runtime
// Which decorators are applied depends on product data and request params
 // All decorators implement PriceCalculator - same interface, different chains
 
@Controller
@RequestMapping("/products")
public class ProductSearchController {

    private final ProductSearchService productSearchService;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    public ProductSearchController(ProductSearchService productSearchService,
                                   ProductService productService,
                                   ReviewService reviewService,
                                   OrderService orderService,
                                   UserRepository userRepository) {
        this.productSearchService = productSearchService;
        this.productService = productService;
        this.reviewService = reviewService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    //Main product listing page
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

    //Search products by keyword
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

    //Advanced search with all filters
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

    //Filter by category only
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

    //View single product details
    @GetMapping("/{productId}")
    public String viewProductDetails(@PathVariable Long productId,
                                     @RequestParam(required = false) String couponCode,
                                     @RequestParam(required = false) Double couponDiscount,
                                     java.security.Principal principal,
                                     Model model) {

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("product", product);

        // start with base price
        PriceCalculator calculator = new BasePriceCalculator(product);

        // If product has a discount, wrap with DiscountDecorator
        // check the product's discountPercent
        if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0) {
            calculator = new DiscountDecorator(calculator, product.getDiscountPercent());
        }

        // apply 18% GST tax 
        calculator = new TaxDecorator(calculator, 18.0);

        // If coupon code, wrap with CouponDecorator
        if (couponCode != null && !couponCode.isEmpty() && couponDiscount != null) {
            calculator = new CouponDecorator(calculator, couponCode, couponDiscount);
        }

        // Call calculatePrice() on outermost decorator
        // This triggers the chain
        BigDecimal finalPrice = calculator.calculatePrice();

        // getDescription() builds the full readable breakdown
        String priceBreakdown = calculator.getDescription();

        model.addAttribute("finalPrice", finalPrice);
        model.addAttribute("priceBreakdown", priceBreakdown);
        model.addAttribute("couponCode", couponCode);


        // Load reviews and average rating from database
        model.addAttribute("reviews", reviewService.getReviewsByProduct(product));
        model.addAttribute("averageRating", reviewService.getAverageRating(product));
        model.addAttribute("productId", productId);

        // Pass logged-in user info for review eligibility
        if (principal != null) {
            var user = userRepository.findByEmail(principal.getName());
            if (user.isPresent()) {
                model.addAttribute("loggedInUser", user.get());

                List<Order> allDeliveredOrders = orderService.getDeliveredOrdersByUser(user.get());
                List<Order> eligibleOrders = allDeliveredOrders.stream()
                        .filter(order -> order.getItems().stream()
                                .anyMatch(item -> item.getProduct().getId().equals(productId)))
                        .toList();
                model.addAttribute("deliveredOrders", eligibleOrders);
            }
        }

        return "products/detail";
    }

    
    @GetMapping("/{productId}/calculate-price")
    @ResponseBody
    public PriceBreakdown calculatePrice(@PathVariable Long productId,
                                         @RequestParam(defaultValue = "true") boolean applyTax,
                                         @RequestParam(required = false) String couponCode,
                                         @RequestParam(required = false) Double couponDiscount) {

        // Fetch real product from database
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));


        //Base
        PriceCalculator calculator = new BasePriceCalculator(product);
        StringBuilder appliedRules = new StringBuilder("Base");

        //  Discount if product has one
        if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0) {
            calculator = new DiscountDecorator(calculator, product.getDiscountPercent());
            appliedRules.append(" + Discount(").append(product.getDiscountPercent()).append("%)");
        }

        // Tax 
        if (applyTax) {
            calculator = new TaxDecorator(calculator, 18.0);
            appliedRules.append(" + Tax(18%)");
        }

        //  Coupon if valid code and discount provided
        if (couponCode != null && !couponCode.isEmpty() && couponDiscount != null && couponDiscount > 0) {
            calculator = new CouponDecorator(calculator, couponCode, couponDiscount);
            appliedRules.append(" + Coupon(").append(couponCode).append(")");
        }

        // Trigger the full chain
        BigDecimal finalPrice = calculator.calculatePrice();
        String breakdown = calculator.getDescription();


        return new PriceBreakdown(finalPrice, breakdown, appliedRules.toString());
    }

    //Featured/promoted products page
    @GetMapping("/featured")
    public String showFeaturedProducts(Model model) {
        List<Product> featuredProducts = productSearchService.getFeaturedProducts(12);
        List<Category> categories = productSearchService.getAllCategories();

        model.addAttribute("products", featuredProducts);
        model.addAttribute("categories", categories);

        return "products/featured";
    }


    static class PriceBreakdown {
        public BigDecimal finalPrice;
        public String breakdown;
        public String appliedRules;

        public PriceBreakdown(BigDecimal finalPrice, String breakdown, String appliedRules) {
            this.finalPrice = finalPrice;
            this.breakdown = breakdown;
            this.appliedRules = appliedRules;
        }
    }
}
