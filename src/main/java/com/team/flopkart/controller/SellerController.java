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
}
