package com.team.flopkart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import com.team.flopkart.dto.CustomerRegistrationDTO;
import com.team.flopkart.dto.SellerForm;
import com.team.flopkart.model.Seller;
import com.team.flopkart.model.UserRole;
import com.team.flopkart.service.SellerService;
import com.team.flopkart.service.UserService;
import com.team.flopkart.model.User;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {

        private final UserService userService;
    private final SellerService sellerService;
    
    public AuthController(UserService userService, SellerService sellerService) {
        this.userService = userService;
        this.sellerService = sellerService;
    }
    
    // ==================== LOGIN PAGES ====================
    
    /**
     * Show customer login page
     */
    @GetMapping("/customer/login")
    public String showCustomerLogin(@RequestParam(required = false) String error,
                                     @RequestParam(required = false) String logout,
                                     Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        model.addAttribute("userType", "customer");
        return "auth/customer-login";
    }
    
    /**
     * Show seller login page
     */
    @GetMapping("/seller/login")
    public String showSellerLogin(@RequestParam(required = false) String error,
                                   @RequestParam(required = false) String logout,
                                   Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        model.addAttribute("userType", "seller");
        return "auth/seller-login";
    }
    
    /**
     * General login page that redirects based on choice
     */
    @GetMapping("/login")
    public String showLoginChoice() {
        return "auth/login-choice";
    }
    
    // ==================== SIGNUP PAGES ====================
    
    /**
     * Show customer registration page
     */
    @GetMapping("/customer/signup")
    public String showCustomerSignup(Model model) {
        model.addAttribute("customer", new CustomerRegistrationDTO());
        return "auth/customer-signup";
    }
    
    /**
     * Process customer registration
     */
    @PostMapping("/customer/signup")
    public String registerCustomer(@Valid @ModelAttribute("customer") CustomerRegistrationDTO dto,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        
        // Validation errors
        if (bindingResult.hasErrors()) {
            return "auth/customer-signup";
        }
        
        // Check password match
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "auth/customer-signup";
        }
        
        // Check if email already exists
        if (userService.emailExists(dto.getEmail())) {
            model.addAttribute("error", "Email already registered");
            return "auth/customer-signup";
        }
        
        try {
            // Create new customer user
            User user = new User();
            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword()); // Will be encoded in service
            user.setPhone(dto.getPhone());
            user.setRole(UserRole.CUSTOMER);
            user.setEnabled(true);
            
            userService.registerUser(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! Please login.");
            
            return "redirect:/auth/customer/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/customer-signup";
        }
    }
    
    /**
     * Show seller registration page
     */
    @GetMapping("/seller/signup")
    public String showSellerSignup(Model model) {
        model.addAttribute("seller", new SellerForm());
        return "auth/seller-signup";
    }
    
    /**
     * Process seller registration
     */
    @PostMapping("/seller/signup")
    public String registerSeller(@Valid @ModelAttribute("seller") SellerForm dto,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        
        // Validation errors
        if (bindingResult.hasErrors()) {
            return "auth/seller-signup";
        }
        
        // Check password match
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "auth/seller-signup";
        }
        
        // Check if email already exists
        if (userService.emailExists(dto.getEmail())) {
            model.addAttribute("error", "Email already registered");
            return "auth/seller-signup";
        }
        
        try {
            // Create new seller user
            User user = new User();
            user.setFullName(dto.getFullName());
            user.setEmail(dto.getEmail());
            user.setPassword(dto.getPassword()); // Will be encoded in service
            user.setPhone(dto.getPhone());
            user.setRole(UserRole.SELLER);
            user.setEnabled(true);
            
            User savedUser = userService.registerUser(user);
            
            // Create seller profile
            Seller seller = new Seller();
            seller.setUser(savedUser);
            seller.setShopName(dto.getShopName());
            seller.setGstNumber(dto.getGstNumber());
            seller.setPhoneNumber(dto.getPhone());
            seller.setBankAccount(dto.getBankAccount());
            seller.setBusinessAddress(dto.getBusinessAddress());
            seller.setShopDescription(dto.getShopDescription());
            seller.setIsVerified(false); // Pending verification
            
            sellerService.registerSeller(seller);
            
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! Your seller account is pending verification. Please login.");
            
            return "redirect:/auth/seller/login";
            
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/seller-signup";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/seller-signup";
        }
    }
    
    /**
     * General signup page that shows choice
     */
    @GetMapping("/signup")
    public String showSignupChoice() {
        return "auth/signup-choice";
    }
}