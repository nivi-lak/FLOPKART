// src/main/java/com/team/flopkart/controller/CartController.java
package com.team.flopkart.controller;

import com.team.flopkart.model.Cart;
import com.team.flopkart.model.User;
import com.team.flopkart.repository.UserRepository;
import com.team.flopkart.service.ICartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Spring MVC Controller for all cart-related HTTP routes.
 *
 * Design Principle — DIP (Dependency Inversion Principle):
 * This controller depends on ICartService (the interface/abstraction),
 * NOT on CartService (the concrete implementation).
 * If CartService is ever replaced, this file needs zero changes.
 *
 * MVC Role: This is the "C" in MVC. It handles HTTP, calls the service,
 * and passes data to Thymeleaf templates (the "V").
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    // ✅ Depend on the INTERFACE, not the concrete class — this IS the DIP demo
    private final ICartService cartService;
    private final UserRepository userRepository;

    // Constructor injection (not @Autowired field injection)
    public CartController(ICartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    /**
     * Extracts the logged-in User from the Principal.
     * Principal is provided automatically by Spring Security.
     * principal.getName() returns the email (our username field).
     */
    private User getLoggedInUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Routes ─────────────────────────────────────────────────────────────

    /**
     * GET /cart  → show the cart page
     * Model is like a map — you put data in, Thymeleaf reads it by key name.
     */
    @GetMapping
    public String viewCart(Model model, Principal principal) {
        User user = getLoggedInUser(principal);
        Cart cart = cartService.getOrCreateCart(user);

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", cart.getSubtotal());

        return "cart/view"; // → templates/cart/view.html
    }

    /**
     * POST /cart/add?productId=5&quantity=2  → add item, then redirect
     * @RequestParam reads values from the URL query string or form fields.
     * RedirectAttributes lets you pass a one-time flash message after redirect.
     */
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(principal);
            cartService.addToCart(user, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect back to whatever product page they came from
        return "redirect:/cart";
    }

    /**
     * POST /cart/update?cartItemId=3&quantity=5  → update qty, redirect
     */
    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long cartItemId,
                                 @RequestParam int quantity,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(principal);
            cartService.updateQuantity(user, cartItemId, quantity);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * POST /cart/remove?cartItemId=3  → remove one item, redirect
     */
    @PostMapping("/remove")
    public String removeItem(@RequestParam Long cartItemId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            User user = getLoggedInUser(principal);
            cartService.removeItem(user, cartItemId);
            redirectAttributes.addFlashAttribute("successMessage", "Item removed.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * POST /cart/clear  → empty the whole cart
     * Member 4 (Order) will also call cartService.clearCart() internally
     * after a successful order placement.
     */
    @PostMapping("/clear")
    public String clearCart(Principal principal) {
        User user = getLoggedInUser(principal);
        cartService.clearCart(user);
        return "redirect:/cart";
    }
}

// CartController.java
// Handles HTTP requests (add/view cart)
// Sends data to UI

// OOAD concept: MVC (Controller) + DIP