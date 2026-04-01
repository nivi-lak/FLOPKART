package com.team.flopkart.config;

import com.team.flopkart.model.*;
import com.team.flopkart.repository.UserRepository;
import com.team.flopkart.pattern.observer.OrderEventPublisher;
import com.team.flopkart.pattern.observer.NotificationObserver;
import com.team.flopkart.pattern.observer.InventoryObserver;
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
    private final OrderEventPublisher orderEventPublisher;
    private final NotificationObserver notificationObserver;
    private final InventoryObserver inventoryObserver;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      OrderEventPublisher orderEventPublisher,
                      NotificationObserver notificationObserver,
                      InventoryObserver inventoryObserver) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderEventPublisher = orderEventPublisher;
        this.notificationObserver = notificationObserver;
        this.inventoryObserver = inventoryObserver;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        registerObservers();
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

    private void registerObservers() {
        orderEventPublisher.addObserver(notificationObserver);
        orderEventPublisher.addObserver(inventoryObserver);
        System.out.println(">> DataSeeder: observers registered.");
    }
}