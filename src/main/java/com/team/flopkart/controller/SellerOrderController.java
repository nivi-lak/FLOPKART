package com.team.flopkart.controller;

import com.team.flopkart.model.Order;
import com.team.flopkart.model.OrderStatus;
import com.team.flopkart.model.Seller;
import com.team.flopkart.model.User;
import com.team.flopkart.service.OrderService;
import com.team.flopkart.service.SellerService;
import com.team.flopkart.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * SellerOrderController — handles seller-facing order view and status update routes.
 * Member 4's responsibility; created here to keep the app functional.
 */
@Controller
@RequestMapping("/seller/orders")
public class SellerOrderController {

    private final OrderService orderService;
    private final SellerService sellerService;
    private final UserService userService;

    public SellerOrderController(OrderService orderService,
                                 SellerService sellerService,
                                 UserService userService) {
        this.orderService = orderService;
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
    public String listOrders(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(required = false) String status,
                             Model model) {
        Seller seller = getSellerForUser(userDetails);
        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersBySellerAndStatus(seller, OrderStatus.valueOf(status));
        } else {
            orders = orderService.getOrdersBySeller(seller);
        }
        model.addAttribute("seller", seller);
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingOrders",   orderService.getOrdersBySellerAndStatus(seller, OrderStatus.PENDING).size());
        model.addAttribute("confirmedOrders", orderService.getOrdersBySellerAndStatus(seller, OrderStatus.CONFIRMED).size());
        model.addAttribute("shippedOrders",   orderService.getOrdersBySellerAndStatus(seller, OrderStatus.SHIPPED).size());
        model.addAttribute("deliveredOrders", orderService.getOrdersBySellerAndStatus(seller, OrderStatus.DELIVERED).size());
        return "seller/orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrderDetails(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Seller seller = getSellerForUser(userDetails);
        Optional<Order> orderOpt = orderService.getOrderByIdOptional(id);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found");
            return "redirect:/seller/orders";
        }
        Order order = orderOpt.get();
        boolean belongsToSeller = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSeller().getId().equals(seller.getId()));
        if (!belongsToSeller) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only view your own orders");
            return "redirect:/seller/orders";
        }
        model.addAttribute("seller", seller);
        model.addAttribute("order", order);
        return "seller/orders/details";
    }

    @PostMapping("/{id}/update-status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        getSellerForUser(userDetails); // security check — must be a seller
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status);
            orderService.updateOrderStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + newStatus);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid order status");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/orders/" + id;
    }
}