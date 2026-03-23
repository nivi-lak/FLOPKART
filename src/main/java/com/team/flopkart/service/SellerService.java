package com.team.flopkart.service;

import com.team.flopkart.dto.SellerForm;
import com.team.flopkart.model.Seller;
import com.team.flopkart.model.User;
import com.team.flopkart.repository.SellerRepository;
import com.team.flopkart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SellerService — Member 2 (Major use case: seller registration + profile).
 *
 * Design Principle: SRP — this service handles only seller profile operations.
 * Product CRUD is ProductService's concern; search is ProductSearchService's concern.
 */
@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    /** Constructor injection — no @Autowired field injection. */
    public SellerService(SellerRepository sellerRepository,
                         UserRepository userRepository) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
    }

    /**
     * Registers a new Seller profile for an already-authenticated User.
     * Throws if the user already has a seller profile or GST is duplicate.
     */
    @Transactional
    public Seller registerSeller(String userEmail, SellerForm form) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        if (sellerRepository.existsByUser(user)) {
            throw new IllegalStateException("Seller profile already exists for this account.");
        }

        if (form.getGstNumber() != null && !form.getGstNumber().isBlank()
                && sellerRepository.existsByGstNumber(form.getGstNumber())) {
            throw new IllegalStateException("A seller with this GST number is already registered.");
        }

        Seller seller = new Seller();
        seller.setUser(user);
        seller.setShopName(form.getShopName());
        seller.setGstNumber(form.getGstNumber());
        seller.setPhoneNumber(form.getPhoneNumber());
        seller.setBankAccount(form.getBankAccount());
        seller.setVerified(false);

        return sellerRepository.save(seller);
    }

    /**
     * Returns the Seller profile for the given authenticated user email.
     * Returns null if no profile exists yet (used to redirect to registration).
     */
    public Seller findByEmail(String email) {
        return sellerRepository.findByUser_Email(email).orElse(null);
    }

    public Seller findById(Long id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found: " + id));
    }

    /**
     * Updates an existing seller's profile fields.
     */
    @Transactional
    public Seller updateProfile(String userEmail, SellerForm form) {
        Seller seller = sellerRepository.findByUser_Email(userEmail)
                .orElseThrow(() -> new IllegalStateException("No seller profile found."));

        seller.setShopName(form.getShopName());
        seller.setPhoneNumber(form.getPhoneNumber());
        seller.setBankAccount(form.getBankAccount());
        // GST intentionally not editable after registration

        return sellerRepository.save(seller);
    }

    public boolean hasSellerProfile(String email) {
        return sellerRepository.findByUser_Email(email).isPresent();
    }
}