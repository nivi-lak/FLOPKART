package com.team.flopkart.controller;

import com.team.flopkart.dto.SellerForm;
import com.team.flopkart.model.Seller;
import com.team.flopkart.service.SellerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * SellerController — Member 2 (Major use case: seller registration + profile).
 *
 * Design Principle: SRP — this controller handles only seller profile web requests.
 * Search requests go to ProductSearchController.
 *
 * Constructor injection used throughout (no @Autowired field injection).
 */
@Controller
@RequestMapping("/seller")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * GET /seller/register — show registration form.
     * Redirects to profile if seller already registered.
     */
    @GetMapping("/register")
    public String showRegisterForm(Principal principal, Model model) {
        if (sellerService.hasSellerProfile(principal.getName())) {
            return "redirect:/seller/profile";
        }
        model.addAttribute("sellerForm", new SellerForm());
        return "seller/register";
    }

    /**
     * POST /seller/register — submit registration form.
     */
    @PostMapping("/register")
    public String submitRegisterForm(@Valid @ModelAttribute("sellerForm") SellerForm form,
                                     BindingResult result,
                                     Principal principal,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "seller/register";
        }
        try {
            sellerService.registerSeller(principal.getName(), form);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Shop registered successfully! Welcome to Flopkart.");
            return "redirect:/seller/profile";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("sellerForm", form);
            return "seller/register";
        }
    }

    /**
     * GET /seller/profile — view seller profile and shop details.
     */
    @GetMapping("/profile")
    public String viewProfile(Principal principal, Model model) {
        Seller seller = sellerService.findByEmail(principal.getName());
        if (seller == null) {
            return "redirect:/seller/register";
        }
        model.addAttribute("seller", seller);
        return "seller/profile";
    }

    /**
     * GET /seller/profile/edit — load edit form pre-filled with current data.
     */
    @GetMapping("/profile/edit")
    public String showEditForm(Principal principal, Model model) {
        Seller seller = sellerService.findByEmail(principal.getName());
        if (seller == null) {
            return "redirect:/seller/register";
        }
        SellerForm form = new SellerForm();
        form.setShopName(seller.getShopName());
        form.setPhoneNumber(seller.getPhoneNumber());
        form.setBankAccount(seller.getBankAccount());
        form.setGstNumber(seller.getGstNumber()); // shown read-only in template
        model.addAttribute("sellerForm", form);
        model.addAttribute("seller", seller);
        return "seller/register"; // reuse same form template
    }

    /**
     * POST /seller/profile/edit — save updated profile.
     */
    @PostMapping("/profile/edit")
    public String submitEditForm(@Valid @ModelAttribute("sellerForm") SellerForm form,
                                 BindingResult result,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "seller/register";
        }
        sellerService.updateProfile(principal.getName(), form);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated.");
        return "redirect:/seller/profile";
    }
}