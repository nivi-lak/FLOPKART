package com.team.flopkart.config;

import com.team.flopkart.model.*;
import com.team.flopkart.repository.UserRepository;
import com.team.flopkart.pattern.observer.OrderEventPublisher;
import com.team.flopkart.pattern.observer.NotificationObserver;
import com.team.flopkart.pattern.observer.InventoryObserver;
import com.team.flopkart.repository.ProductRepository;
import com.team.flopkart.repository.CategoryRepository;
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
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      OrderEventPublisher orderEventPublisher,
                      NotificationObserver notificationObserver,
                      InventoryObserver inventoryObserver,
                      ProductRepository productRepository,
                      CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderEventPublisher = orderEventPublisher;
        this.notificationObserver = notificationObserver;
        this.inventoryObserver = inventoryObserver;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedCategoriesAndProducts();
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

    private void seedCategoriesAndProducts() {
        if (productRepository.count() > 0) return; // don't seed twice

        // Get the demo seller
        User seller = userRepository.findByEmail("seller@flopkart.com").orElse(null);
        if (seller == null) return;

        // Create categories
        Category electronics = new Category("Electronics", "Electronic devices and gadgets");
        electronics = categoryRepository.save(electronics);

        Category clothing = new Category("Clothing", "Fashion and apparel");
        clothing = categoryRepository.save(clothing);

        // Create products
        Product phone = new Product("iPhone 15", 79999.00, 50);
        phone.setDescription("Latest iPhone with advanced features");
        phone.setBrand("Apple");
        phone.setCategory(electronics);
        phone.setSeller(seller);
        phone.setDiscountPercent(10);
        productRepository.save(phone);

        Product laptop = new Product("MacBook Pro", 129999.00, 20);
        laptop.setDescription("Professional laptop for developers");
        laptop.setBrand("Apple");
        laptop.setCategory(electronics);
        laptop.setSeller(seller);
        laptop.setDiscountPercent(5);
        productRepository.save(laptop);

        Product shirt = new Product("Cotton T-Shirt", 999.00, 100);
        shirt.setDescription("Comfortable cotton t-shirt");
        shirt.setBrand("Generic");
        shirt.setCategory(clothing);
        shirt.setSeller(seller);
        shirt.setDiscountPercent(0);
        productRepository.save(shirt);

        System.out.println(">> DataSeeder: categories and products seeded.");
    }

    private void registerObservers() {
        orderEventPublisher.addObserver(notificationObserver);
        orderEventPublisher.addObserver(inventoryObserver);
        System.out.println(">> DataSeeder: observers registered.");
    }
}