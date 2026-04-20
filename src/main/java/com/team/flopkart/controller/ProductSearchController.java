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
 *
 * DECORATOR PATTERN USAGE:
 * - calculatePrice() endpoint builds the chain at runtime
 * - Which decorators are applied depends on product data and request params
 * - All decorators implement PriceCalculator — same interface, different chains
 */
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
     * Builds the decorator chain and passes price breakdown to the view.
     * Chain is built at runtime based on product data and coupon param.
     */
    @GetMapping("/{productId}")
    public String viewProductDetails(@PathVariable Long productId,
                                     @RequestParam(required = false) String couponCode,
                                     @RequestParam(required = false) Double couponDiscount,
                                     java.security.Principal principal,
                                     Model model) {

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("product", product);

        // -------------------------------------------------------
        // DECORATOR PATTERN — build price chain at runtime
        // -------------------------------------------------------

        // Step 1: Always start with base price (innermost layer)
        PriceCalculator calculator = new BasePriceCalculator(product);

        // Step 2: If product has a discount, wrap with DiscountDecorator
        // Decision made at runtime by checking the product's discountPercent
        if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0) {
            calculator = new DiscountDecorator(calculator, product.getDiscountPercent());
        }

        // Step 3: Always apply 18% GST tax (Indian standard)
        // Wrap on top of whatever chain exists so far
        calculator = new TaxDecorator(calculator, 18.0);

        // Step 4: If user provided a coupon code, wrap with CouponDecorator
        // Decision made at runtime from the request parameter
        if (couponCode != null && !couponCode.isEmpty() && couponDiscount != null) {
            calculator = new CouponDecorator(calculator, couponCode, couponDiscount);
        }

        // Step 5: Call calculatePrice() on outermost decorator
        // This triggers the chain: Coupon → Tax → Discount → Base
        // Each layer delegates inward, result bubbles back outward
        BigDecimal finalPrice = calculator.calculatePrice();

        // Step 6: getDescription() builds the full readable breakdown
        // e.g. "Base Price: ₹1000 → Discount (10%): ₹900 → Tax (18%): ₹1062 → Coupon 'SAVE20': ₹849.60"
        String priceBreakdown = calculator.getDescription();

        model.addAttribute("finalPrice", finalPrice);
        model.addAttribute("priceBreakdown", priceBreakdown);
        model.addAttribute("couponCode", couponCode);

        // -------------------------------------------------------

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

    /**
     * AJAX endpoint to calculate price with decorators dynamically.
     *
     * DECORATOR PATTERN — same chain building logic as viewProductDetails
     * but returns JSON instead of a view, for dynamic price updates on the page.
     *
     * Example call:
     * GET /products/42/calculate-price?applyTax=true&couponCode=SAVE20&couponDiscount=20
     *
     * Example response:
     * {
     *   "finalPrice": 849.60,
     *   "breakdown": "Base Price: ₹1000.00 → Discount (10%): ₹900.00 → Tax (18.0%, ₹162.00): ₹1062.00 → Coupon 'SAVE20' (20.0%, -₹212.40): ₹849.60",
     *   "appliedRules": "Base + Discount + Tax + Coupon"
     * }
     */
    @GetMapping("/{productId}/calculate-price")
    @ResponseBody
    public PriceBreakdown calculatePrice(@PathVariable Long productId,
                                         @RequestParam(defaultValue = "true") boolean applyTax,
                                         @RequestParam(required = false) String couponCode,
                                         @RequestParam(required = false) Double couponDiscount) {

        // Fetch real product from database
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // -------------------------------------------------------
        // DECORATOR PATTERN — runtime chain construction
        // -------------------------------------------------------

        // Layer 1: Base — always present
        PriceCalculator calculator = new BasePriceCalculator(product);
        StringBuilder appliedRules = new StringBuilder("Base");

        // Layer 2: Discount — only if product has one
        if (product.getDiscountPercent() != null && product.getDiscountPercent() > 0) {
            calculator = new DiscountDecorator(calculator, product.getDiscountPercent());
            appliedRules.append(" + Discount(").append(product.getDiscountPercent()).append("%)");
        }

        // Layer 3: Tax — based on request param (default true)
        if (applyTax) {
            calculator = new TaxDecorator(calculator, 18.0);
            appliedRules.append(" + Tax(18%)");
        }

        // Layer 4: Coupon — only if valid code and discount provided
        if (couponCode != null && !couponCode.isEmpty() && couponDiscount != null && couponDiscount > 0) {
            calculator = new CouponDecorator(calculator, couponCode, couponDiscount);
            appliedRules.append(" + Coupon(").append(couponCode).append(")");
        }

        // Trigger the full chain — outermost calls inward, result bubbles out
        BigDecimal finalPrice = calculator.calculatePrice();
        String breakdown = calculator.getDescription();

        // -------------------------------------------------------

        return new PriceBreakdown(finalPrice, breakdown, appliedRules.toString());
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

    /**
     * Response object for the calculate-price AJAX endpoint.
     *
     * finalPrice     — the computed final price after all decorators
     * breakdown      — human-readable chain e.g. "Base → Discount → Tax → Coupon"
     * appliedRules   — summary of which decorators were applied at runtime
     */
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