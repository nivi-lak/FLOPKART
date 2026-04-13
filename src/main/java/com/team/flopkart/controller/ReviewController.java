package com.team.flopkart.controller;

import com.team.flopkart.model.*;
import com.team.flopkart.service.ReviewService;
import com.team.flopkart.service.OrderService;
import com.team.flopkart.dto.ReviewForm;
import com.team.flopkart.repository.UserRepository;
import com.team.flopkart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import java.security.Principal;

/**
 * Controller for Review operations.
 * Demonstrates LSP: handles review requests without breaking substitutability.
 */
@Controller
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ReviewController(ReviewService reviewService, OrderService orderService,
                           UserRepository userRepository, ProductRepository productRepository) {
        this.reviewService = reviewService;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    private User getLoggedInUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/form/{productId}/{orderId}")
    public String showReviewForm(@PathVariable Long productId, @PathVariable Long orderId,
                                Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = getLoggedInUser(principal);
        Order order = orderService.getOrderById(orderId);

        // Validate that user can review this product
        if (order == null || !order.getUser().equals(user) || order.getStatus() != OrderStatus.DELIVERED) {
            return "redirect:/orders/history";
        }

        boolean productInOrder = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
        if (!productInOrder) {
            return "redirect:/orders/history";
        }

        Product product = order.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst().get().getProduct();

        ReviewForm reviewForm = new ReviewForm(productId, orderId, null, null);
        model.addAttribute("reviewForm", reviewForm);
        model.addAttribute("product", product);
        model.addAttribute("order", order);
        return "review/form";
    }

    @PostMapping("/submit")
    public String submitReview(Principal principal, @Valid @ModelAttribute ReviewForm reviewForm,
                              BindingResult result, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = getLoggedInUser(principal);

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Please correct the errors below");
            return "redirect:/reviews/form/" + reviewForm.getProductId() + "/" + reviewForm.getOrderId();
        }

        try {
            Review review = reviewService.createReview(user, reviewForm.getProductId(),
                    reviewForm.getOrderId(), reviewForm.getRating(), reviewForm.getComment());
            redirectAttributes.addFlashAttribute("success", "Review submitted successfully!");
            return "redirect:/products/" + reviewForm.getProductId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reviews/form/" + reviewForm.getProductId() + "/" + reviewForm.getOrderId();
        }
    }
}
