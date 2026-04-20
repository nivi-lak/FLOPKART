package com.team.flopkart.service;

import com.team.flopkart.model.Seller;
import com.team.flopkart.model.User;
import com.team.flopkart.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.util.List;
import java.util.Optional;
 

//Service for seller registration, profile management, and verification.

@Service
@Transactional
public class SellerService {
    
    private final SellerRepository sellerRepository;
    
    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }
    
    //Register a new seller profile for a user with SELLER role

    public Seller registerSeller(Seller seller) {
        // Validate user has SELLER role
        if (seller.getUser() == null) {
            throw new IllegalArgumentException("User must be associated with seller");
        }
        
        // Check if seller already exists for this user
        if (sellerRepository.existsByUser(seller.getUser())) {
            throw new IllegalStateException("Seller profile already exists for this user");
        }
        
        // Check GST uniqueness
        if (sellerRepository.findByGstNumber(seller.getGstNumber()).isPresent()) {
            throw new IllegalStateException("GST number already registered");
        }
        
        // New sellers start unverified
        seller.setIsVerified(false);
        
        return sellerRepository.save(seller);
    }
    
    //Update seller profile information
     */
    public Seller updateSellerProfile(Long sellerId, Seller updatedInfo) {
        Seller existing = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found"));
        
        // Update allowed fields (not user or verification status)
        existing.setShopName(updatedInfo.getShopName());
        existing.setPhoneNumber(updatedInfo.getPhoneNumber());
        existing.setBankAccount(updatedInfo.getBankAccount());
        existing.setShopDescription(updatedInfo.getShopDescription());
        existing.setShopLogoUrl(updatedInfo.getShopLogoUrl());
        existing.setBusinessAddress(updatedInfo.getBusinessAddress());
        
        // GST number change requires admin verification
        if (!existing.getGstNumber().equals(updatedInfo.getGstNumber())) {
            if (sellerRepository.findByGstNumber(updatedInfo.getGstNumber()).isPresent()) {
                throw new IllegalStateException("GST number already in use");
            }
            existing.setGstNumber(updatedInfo.getGstNumber());
            existing.setIsVerified(false); // Re-verification needed
        }
        
        return sellerRepository.save(existing);
    }
    
    //Get seller profile by user
    public Optional<Seller> getSellerByUser(User user) {
        return sellerRepository.findByUser(user);
    }
    
    // Get seller profile by user ID
    public Optional<Seller> getSellerByUserId(Long userId) {
        return sellerRepository.findByUserId(userId);
    }
    
    //Get seller by ID
    public Optional<Seller> getSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId);
    }
    
    //Verify a seller (admin function)
    public void verifySeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found"));
        
        seller.verify();
        sellerRepository.save(seller);
    }
    
    //Unverify a seller (admin function)
    public void unverifySeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found"));
        
        seller.unverify();
        sellerRepository.save(seller);
    }
    
    //Get all verified sellers
    public List<Seller> getVerifiedSellers() {
        return sellerRepository.findByIsVerifiedTrue();
    }
    
    // Get all pending verification sellers (admin)
    public List<Seller> getPendingVerificationSellers() {
        return sellerRepository.findByIsVerifiedFalse();
    }
    
    //Search sellers by shop name
    public List<Seller> searchSellersByShopName(String shopName) {
        return sellerRepository.findByShopNameContainingIgnoreCase(shopName);
    }
    
    //Check if user has seller profile
    public boolean hasSellerProfile(User user) {
        return sellerRepository.existsByUser(user);
    }
}
