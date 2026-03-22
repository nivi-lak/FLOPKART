package com.team.flopkart.config;

import com.team.flopkart.model.*;
import com.team.flopkart.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data on startup so the app is ready to demo immediately.
 * Each member should add their own seed data in their section below.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        // Member 1: seedCategories(), seedProducts()
        // Member 2: seedSellers()
        // Member 3: nothing needed (carts created at runtime)
        // Member 4: nothing needed (orders created at runtime)
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return; // don't seed twice

        // Admin
        User admin = new User();
        admin.setFullName("Admin User");
        admin.setEmail("admin@flopkart.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        // Demo customer
        User customer = new User();
        customer.setFullName("Demo Customer");
        customer.setEmail("customer@flopkart.com");
        customer.setPassword(passwordEncoder.encode("customer123"));
        customer.setRole(UserRole.CUSTOMER);
        customer.setEnabled(true);
        userRepository.save(customer);

        // Demo seller
        User seller = new User();
        seller.setFullName("Demo Seller");
        seller.setEmail("seller@flopkart.com");
        seller.setPassword(passwordEncoder.encode("seller123"));
        seller.setRole(UserRole.SELLER);
        seller.setEnabled(true);
        userRepository.save(seller);

        System.out.println(">> DataSeeder: users seeded.");
    }
}