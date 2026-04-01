package com.team.flopkart.controller;

import com.team.flopkart.model.*;
import com.team.flopkart.service.OrderService;
import com.team.flopkart.service.CartService;
import com.team.flopkart.dto.CheckoutForm;
import com.team.flopkart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * Controller for Order operations.
 * Demonstrates DIP: depends on OrderService interface.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final UserRepository userRepository;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService, UserRepository userRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/checkout")
    public String showCheckout(Principal principal, Model model) {
        User user = getLoggedInUser(principal);
        Cart cart = cartService.getCartByUser(user);
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("checkoutForm", new CheckoutForm());
        return "order/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(Principal principal, @ModelAttribute CheckoutForm checkoutForm, RedirectAttributes redirectAttributes) {
        User user = getLoggedInUser(principal);
        Cart cart = cartService.getCartByUser(user);
        if (cart == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Cart is empty");
            return "redirect:/cart";
        }

        try {
            Order order = orderService.createOrder(user, cart.getItems(), checkoutForm.getShippingAddress(), checkoutForm.getShippingCity(), checkoutForm.getShippingPincode());
            cartService.clearCart(user);
            redirectAttributes.addFlashAttribute("success", "Order placed successfully! Order Number: " + order.getOrderNumber());
            return "redirect:/orders/confirmation/" + order.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to place order: " + e.getMessage());
            return "redirect:/orders/checkout";
        }
    }

    @GetMapping("/confirmation/{id}")
    public String showConfirmation(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            return "redirect:/orders/history";
        }
        model.addAttribute("order", order);
        return "order/confirmation";
    }

    @GetMapping("/history")
    public String showOrderHistory(Principal principal, Model model) {
        User user = getLoggedInUser(principal);
        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("orders", orders);
        return "order/history";
    }

    @GetMapping("/track/{orderNumber}")
    public String trackOrder(@PathVariable String orderNumber, Model model) {
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        if (order == null) {
            model.addAttribute("error", "Order not found");
            return "order/track";
        }
        model.addAttribute("order", order);
        return "order/track";
    }

    @PostMapping("/track")
    public String trackOrderPost(@RequestParam String orderNumber) {
        return "redirect:/orders/track/" + orderNumber;
    }
}
