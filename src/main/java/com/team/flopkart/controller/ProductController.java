package com.team.flopkart.controller;

import com.team.flopkart.dto.ProductForm;
import com.team.flopkart.model.Category;
import com.team.flopkart.model.Product;
import com.team.flopkart.model.Seller;
import com.team.flopkart.model.User;
import com.team.flopkart.service.ProductService;
import com.team.flopkart.service.SellerService;
import com.team.flopkart.service.UserService;
import com.team.flopkart.pattern.builder.ProductBuilder;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * ProductController — handles seller product CRUD routes.
 * Member 1's responsibility; created here to keep the app functional.
 */
@Controller
@RequestMapping("/seller/products")
public class ProductController {

    private final ProductService productService;
    private final SellerService sellerService;
    private final UserService userService;

    public ProductController(ProductService productService,
                             SellerService sellerService,
                             UserService userService) {
        this.productService = productService;
        this.sellerService = sellerService;
        this.userService = userService;
    }

    private Seller getSellerForUser(UserDetails userDetails) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return sellerService.getSellerByUser(user)
                .orElseThrow(() -> new IllegalStateException("Seller profile not found"));
    }

    @GetMapping
    public String listProducts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Seller seller = getSellerForUser(userDetails);
        List<Product> products = productService.getProductsBySeller(seller);
        model.addAttribute("seller", seller);
        model.addAttribute("products", products);
        return "seller/products/list";
    }

    @GetMapping("/add")
    public String showAddProductForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Seller seller = getSellerForUser(userDetails);
        List<Category> categories = productService.getAllCategories();
        model.addAttribute("seller", seller);
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);
        return "seller/products/form";
    }

    @PostMapping("/add")
    public String addProduct(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute("productForm") ProductForm productForm,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        Seller seller = getSellerForUser(userDetails);
        if (bindingResult.hasErrors()) {
            model.addAttribute("seller", seller);
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("isEdit", false);
            return "seller/products/form";
        }
        try {
            Product product = new ProductBuilder()
            .name(productForm.getName())
            .description(productForm.getDescription())
            .price(productForm.getPrice()) // since builder takes double
            .discount(productForm.getDiscountPercent())
            .stock(productForm.getStockQuantity())
            .brand(productForm.getBrand())
            .imageUrl(productForm.getImageUrl())
            .active(productForm.getActive())
            .seller(seller)
            .build();
            // Product product = new Product();
            // product.setName(productForm.getName());
            // product.setDescription(productForm.getDescription());
            // product.setPrice(productForm.getPrice());
            // product.setDiscountPercent(productForm.getDiscountPercent());
            // product.setStockQuantity(productForm.getStockQuantity());
            // product.setImageUrl(productForm.getImageUrl());
            // product.setBrand(productForm.getBrand());
            // product.setActive(productForm.getActive());
            productService.createProduct(product, seller, productForm.getCategoryId());
            redirectAttributes.addFlashAttribute("successMessage", "Product added successfully!");
            return "redirect:/seller/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to add product: " + e.getMessage());
            model.addAttribute("seller", seller);
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("isEdit", false);
            return "seller/products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditProductForm(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Seller seller = getSellerForUser(userDetails);
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found");
            return "redirect:/seller/products";
        }
        Product product = productOpt.get();
        if (!product.getSeller().getId().equals(seller.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit your own products");
            return "redirect:/seller/products";
        }
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
        model.addAttribute("seller", seller);
        model.addAttribute("productForm", productForm);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("isEdit", true);
        return "seller/products/form";
    }

    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @Valid @ModelAttribute("productForm") ProductForm productForm,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        Seller seller = getSellerForUser(userDetails);
        if (bindingResult.hasErrors()) {
            model.addAttribute("seller", seller);
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("isEdit", true);
            return "seller/products/form";
        }
        try {
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
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/seller/products";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/seller/products";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to update product: " + e.getMessage());
            model.addAttribute("seller", seller);
            model.addAttribute("categories", productService.getAllCategories());
            model.addAttribute("isEdit", true);
            return "seller/products/form";
        }
    }

    @PostMapping("/toggle/{id}")
    public String toggleProductStatus(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        Seller seller = getSellerForUser(userDetails);
        try {
            productService.toggleActiveStatus(id, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Product status updated");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        Seller seller = getSellerForUser(userDetails);
        try {
            productService.deleteProduct(id, seller);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/products";
    }
}