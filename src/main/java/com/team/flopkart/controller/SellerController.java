package com.team.flopkart.controller;

import com.team.flopkart.dto.ProductForm;
import com.team.flopkart.model.Product;
import com.team.flopkart.model.Seller;
import com.team.flopkart.model.Category;
import com.team.flopkart.model.OrderStatus;
import com.team.flopkart.model.User;
import com.team.flopkart.model.UserRole;
import com.team.flopkart.model.Order;
import com.team.flopkart.service.OrderService;
import com.team.flopkart.service.ProductService;
import com.team.flopkart.service.SellerService;
import com.team.flopkart.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
import java.util.Optional;
import java.math.BigDecimal;
import java.util.List;

/**
 * MEMBER 2 - MAJOR USE CASE CONTROLLER
 * 
 * Controller for seller registration, profile management, and dashboard.
 * 
 * USE CASE FLOWS:
 * 1. Seller Registration Flow:
 *    - User registers with SELLER role
 *    - After login, redirected to complete seller profile
 *    - Enters shop details, GST, bank info
 *    - Profile created (unverified)
 * 
 * 2. Seller Profile Management:
 *    - View own profile
 *    - Edit shop details
 *    - View verification status
 * 
 * 3. Seller Dashboard:
 *    - Overview of shop
 *    - Link to manage products (Member 1's feature)
 *    - View orders (Member 4's feature)
 */
@Controller
@RequestMapping("/seller")
public class SellerController {
    
    private final SellerService sellerService;
    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    public SellerController(SellerService sellerService, UserService userService, ProductService productService , OrderService orderService) {
        this.sellerService = sellerService;
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }
    
    /**
     * Show seller dashboard (landing page after seller login)
     */
    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        // Check if seller profile exists
        Optional<Seller> sellerOpt = sellerService.getSellerByUser(user);
        
        if (sellerOpt.isEmpty()) {
            // No seller profile yet - redirect to registration
            return "redirect:/seller/register";
        }
        
        Seller seller = sellerOpt.get();
        List<Order> orders = orderService.getOrdersBySeller(seller);

        long totalOrders = orders.size();

                List<Product> products = productService.getProductsBySeller(seller);
        long totalProducts = products.size();
        long activeProducts = products.stream()
                                  .filter(Product::getActive)
                                  .count();
        BigDecimal totalRevenue = orders.stream()
            .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("seller", seller);
        model.addAttribute("user", user);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        return "seller/dashboard";
    }
    
    /**
     * Show seller registration form
     * (Called after user with SELLER role first logs in)
     */
    @GetMapping("/register")
    public String showRegistrationForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        // Check if user has SELLER role
        if (user.getRole() != UserRole.SELLER) {
            return "redirect:/";
        }
        
        // Check if seller profile already exists
        if (sellerService.hasSellerProfile(user)) {
            return "redirect:/seller/dashboard";
        }
        
        Seller seller = new Seller();
        seller.setUser(user);
        model.addAttribute("seller", seller);
        model.addAttribute("user", user);
        
        return "seller/register";
    }
    
    /**
     * Process seller registration
     */
    @PostMapping("/register")
    public String registerSeller(@AuthenticationPrincipal UserDetails userDetails,
                                  @Valid @ModelAttribute("seller") Seller seller,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            return "seller/register";
        }
        
        try {
            // Associate with current user
            seller.setUser(user);
            
            // Register seller
            Seller savedSeller = sellerService.registerSeller(seller);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Seller profile created successfully! Your account is pending verification.");
            
            return "redirect:/seller/dashboard";
            
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", user);
            return "seller/register";
        }
    }
    
    /**
     * Show seller profile page
     */
    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        model.addAttribute("seller", seller);
        model.addAttribute("user", user);
        model.addAttribute("isEditMode", false);
        
        return "seller/profile";
    }
    
    /**
     * Show edit profile form
     */
    @GetMapping("/profile/edit")
    public String showEditProfileForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        model.addAttribute("seller", seller);
        model.addAttribute("user", user);
        model.addAttribute("isEditMode", true);
        
        return "seller/profile";
    }
    
    /**
     * Update seller profile
     */
    @PostMapping("/profile/edit")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("seller") Seller updatedSeller,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller existingSeller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("isEditMode", true);
            return "seller/profile";
        }
        
        try {
            sellerService.updateSellerProfile(existingSeller.getId(), updatedSeller);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Profile updated successfully!");
            
            return "redirect:/seller/profile";
            
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("isEditMode", true);
            return "seller/profile";
        }
    }
    
    /**
     * Show verification status page
     */
    @GetMapping("/verification")
    public String showVerificationStatus(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        model.addAttribute("seller", seller);
        model.addAttribute("user", user);
        
        return "seller/verification-status";
    }

    @GetMapping("/products")
    public String listProducts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        List<Product> products = productService.getProductsBySeller(seller);
        
        model.addAttribute("seller", seller);
        model.addAttribute("user", user);
        model.addAttribute("products", products);
        
        return "seller/products/list";
    }
    @GetMapping("/products/add")
    public String showAddProductForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        // Check if seller is verified
        // if (!seller.getIsVerified()) {
        //     model.addAttribute("warning", "Your seller account is pending verification. " +
        //         "You can add products, but they won't be visible to customers until you're verified.");
        // }
        
        ProductForm productForm = new ProductForm();
        List<Category> categories = productService.getAllCategories();
        
        model.addAttribute("seller", seller);
        model.addAttribute("user", user);
        model.addAttribute("productForm", productForm);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);
        
        return "seller/products/form";
    }
    
    /**
     * Process add product form
     */
    @PostMapping("/products/add")
    public String addProduct(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute("productForm") ProductForm productForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        if (bindingResult.hasErrors()) {
            List<Category> categories = productService.getAllCategories();
            model.addAttribute("seller", seller);
            model.addAttribute("user", user);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", false);
            return "seller/products/form";
        }
        
        try {
            // Create product from form
            Product product = new Product();
            product.setName(productForm.getName());
            product.setDescription(productForm.getDescription());
            product.setPrice(productForm.getPrice());
            product.setDiscountPercent(productForm.getDiscountPercent());
            product.setStockQuantity(productForm.getStockQuantity());
            product.setImageUrl(productForm.getImageUrl());
            product.setBrand(productForm.getBrand());
            product.setActive(productForm.getActive());
            
            productService.createProduct(product, seller, productForm.getCategoryId());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product added successfully!");
            
            return "redirect:/seller/products";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to add product: " + e.getMessage());
            List<Category> categories = productService.getAllCategories();
            model.addAttribute("seller", seller);
            model.addAttribute("user", user);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", false);
            return "seller/products/form";
        }
    }
    
    /**
     * Show edit product form
     */
    @GetMapping("/products/edit/{id}")
    public String showEditProductForm(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found");
            return "redirect:/seller/products";
        }
        
        Product product = productOpt.get();
        
        // Security check - only owner can edit
        if (!product.getSeller().getId().equals(seller.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own products");
            return "redirect:/seller/products";
        }
        
        // Convert Product to ProductForm
        ProductForm productForm = new ProductForm();
        productForm.setId(product.getId());
        productForm.setName(product.getName());
        productForm.setDescription(product.getDescription());
        productForm.setPrice(product.getPrice());
        productForm.setDiscountPercent(product.getDiscountPercent());
        productForm.setStockQuantity(product.getStockQuantity());
        productForm.setImageUrl(product.getImageUrl());
        productForm.setBrand(product.getBrand());
        productForm.setActive(product.getActive());
        productForm.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        
        List<Category> categories = productService.getAllCategories();
        
        model.addAttribute("seller", seller);
        model.addAttribute("user", user);
        model.addAttribute("productForm", productForm);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", true);
        
        return "seller/products/form";
    }
    
    /**
     * Process edit product form
     */
    @PostMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("productForm") ProductForm productForm,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        if (bindingResult.hasErrors()) {
            List<Category> categories = productService.getAllCategories();
            model.addAttribute("seller", seller);
            model.addAttribute("user", user);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", true);
            return "seller/products/form";
        }
        
        try {
            // Update product from form
            Product product = new Product();
            product.setName(productForm.getName());
            product.setDescription(productForm.getDescription());
            product.setPrice(productForm.getPrice());
            product.setDiscountPercent(productForm.getDiscountPercent());
            product.setStockQuantity(productForm.getStockQuantity());
            product.setImageUrl(productForm.getImageUrl());
            product.setBrand(productForm.getBrand());
            product.setActive(productForm.getActive());
            
            productService.updateProduct(id, product, seller, productForm.getCategoryId());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product updated successfully!");
            
            return "redirect:/seller/products";
            
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/seller/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update product: " + e.getMessage());
            List<Category> categories = productService.getAllCategories();
            model.addAttribute("seller", seller);
            model.addAttribute("user", user);
            model.addAttribute("categories", categories);
            model.addAttribute("isEdit", true);
            return "seller/products/form";
        }
    }
    
    /**
     * Toggle product active/inactive status
     */
    @PostMapping("/products/toggle/{id}")
    public String toggleProductStatus(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirectAttributes) {
        
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        try {
            productService.toggleActiveStatus(id, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Product status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/seller/products";
    }
    
    /**
     * Delete product (soft delete)
     */
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        
        User user = userService.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Seller seller = sellerService.getSellerByUser(user)
            .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
        
        try {
            productService.deleteProduct(id, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/seller/products";
    }

    @GetMapping("/orders")
public String listOrders(@AuthenticationPrincipal UserDetails userDetails, 
                         @RequestParam(required = false) String status,
                         Model model) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalStateException("User not found"));
    
    Seller seller = sellerService.getSellerByUser(user)
        .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
    
    List<Order> orders;
    if (status != null && !status.isEmpty()) {
        // Filter by status
        OrderStatus orderStatus = OrderStatus.valueOf(status);
        orders = orderService.getOrdersBySellerAndStatus(seller, orderStatus);
    } else {
        // Get all orders
        orders = orderService.getOrdersBySeller(seller);
    }
    
    // Get order statistics
    long pendingOrders = orderService.getOrdersBySellerAndStatus(seller, OrderStatus.PENDING).size();
    long confirmedOrders = orderService.getOrdersBySellerAndStatus(seller, OrderStatus.CONFIRMED).size();
    long shippedOrders = orderService.getOrdersBySellerAndStatus(seller, OrderStatus.SHIPPED).size();
    long deliveredOrders = orderService.getOrdersBySellerAndStatus(seller, OrderStatus.DELIVERED).size();
    
    model.addAttribute("seller", seller);
    model.addAttribute("user", user);
    model.addAttribute("orders", orders);
    model.addAttribute("selectedStatus", status);
    model.addAttribute("pendingOrders", pendingOrders);
    model.addAttribute("confirmedOrders", confirmedOrders);
    model.addAttribute("shippedOrders", shippedOrders);
    model.addAttribute("deliveredOrders", deliveredOrders);
    
    return "seller/orders/list";
}
 
/**
 * View order details
 */
@GetMapping("/orders/{id}")
public String viewOrderDetails(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model,
                                RedirectAttributes redirectAttributes) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalStateException("User not found"));
    
    Seller seller = sellerService.getSellerByUser(user)
        .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
    
    Optional<Order> orderOpt = orderService.getOrderByIdOptional(id);
    
    if (orderOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("errorMessage", "Order not found");
        return "redirect:/seller/orders";
    }
    
    Order order = orderOpt.get();
    
    // Security check - verify order belongs to this seller's products
    boolean belongsToSeller = order.getItems().stream()
        .anyMatch(item -> item.getProduct().getSeller().getId().equals(seller.getId()));
    
    if (!belongsToSeller) {
        redirectAttributes.addFlashAttribute("errorMessage", "You can only view your own orders");
        return "redirect:/seller/orders";
    }
    
    model.addAttribute("seller", seller);
    model.addAttribute("user", user);
    model.addAttribute("order", order);
    
    return "seller/orders/details";
}
 
/**
 * Update order status
 */
@PostMapping("/orders/{id}/update-status")
public String updateOrderStatus(@PathVariable Long id,
                                 @RequestParam String status,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
    User user = userService.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new IllegalStateException("User not found"));
    
    Seller seller = sellerService.getSellerByUser(user)
        .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
    
    try {
        OrderStatus newStatus = OrderStatus.valueOf(status);
        orderService.updateOrderStatus(id, newStatus);
        
        redirectAttributes.addFlashAttribute("successMessage", 
            "Order status updated to " + newStatus);
        
        return "redirect:/seller/orders/" + id;
        
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid order status");
        return "redirect:/seller/orders/" + id;
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/seller/orders/" + id;
    }
}
}
