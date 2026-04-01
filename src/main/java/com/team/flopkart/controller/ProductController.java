package com.team.flopkart.controller;

import com.team.flopkart.model.Product;
import com.team.flopkart.model.User;
import com.team.flopkart.repository.UserRepository;
import com.team.flopkart.repository.ProductRepository;
import com.team.flopkart.service.ProductService;
import com.team.flopkart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.getDemoProducts();
        model.addAttribute("products", products);
        return "product/demo-list";
    }

    /**
     * Demo endpoint for testing Member 4 (Order & Review) functionality.
     * Shows demo products page where user can add items to cart manually.
     */
    @GetMapping("/test/demo-checkout")
    public String demoDemoCheckout(Model model) {
        try {
            List<Product> demoProducts = productService.getDemoProducts();
            model.addAttribute("products", demoProducts);
            return "product/demo-list";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load demo products: " + e.getMessage());
            return "index";
        }
    }
}
