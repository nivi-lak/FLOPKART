package com.team.flopkart.controller;

import com.team.flopkart.model.Product;
import com.team.flopkart.model.User;
import com.team.flopkart.repository.UserRepository;
import com.team.flopkart.repository.ProductRepository;
import com.team.flopkart.service.ProductService;
import com.team.flopkart.service.CartService;
import com.team.flopkart.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    public ProductController(ProductService productService,
                             CartService cartService,
                             ProductRepository productRepository,
                             UserRepository userRepository,
                             ReviewService reviewService) {
        this.productService = productService;
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviewService.getReviewsByProduct(product));
        model.addAttribute("averageRating", reviewService.getAverageRating(product));
        return "product/detail";
    }
}
