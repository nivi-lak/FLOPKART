package com.team.flopkart.config;

import com.team.flopkart.model.*;
import com.team.flopkart.repository.UserRepository;
import com.team.flopkart.pattern.observer.OrderEventPublisher;
import com.team.flopkart.pattern.observer.NotificationObserver;
import com.team.flopkart.pattern.observer.InventoryObserver;
import com.team.flopkart.repository.ProductRepository;
import com.team.flopkart.repository.CategoryRepository;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.team.flopkart.repository.SellerRepository;
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
    private final SellerRepository sellerRepository;

    public DataSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      OrderEventPublisher orderEventPublisher,
                      NotificationObserver notificationObserver,
                      InventoryObserver inventoryObserver,
                      ProductRepository productRepository,
                      CategoryRepository categoryRepository,
                      SellerRepository sellerRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderEventPublisher = orderEventPublisher;
        this.notificationObserver = notificationObserver;
        this.inventoryObserver = inventoryObserver;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.sellerRepository = sellerRepository;
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

        Seller sellerProfile = new Seller();
        sellerProfile.setUser(seller);
        sellerProfile.setShopName("Demo Shop");
        sellerProfile.setGstNumber("29ABCDE1234F1Z5"); // valid format
        sellerProfile.setPhoneNumber("9876543210");
        sellerProfile.setBankAccount("1234567890");
        sellerProfile.setBusinessAddress("Bangalore");

        sellerRepository.save(sellerProfile);
        System.out.println(">> DataSeeder: users seeded.");
    }

    private void seedCategoriesAndProducts() {
        if (productRepository.count() > 0) return; // don't seed twice

        // Get the demo seller
        User sellerUser = userRepository.findByEmail("seller@flopkart.com").orElse(null);
        if (sellerUser == null) return;

        Seller seller = sellerRepository.findByUser(sellerUser)
        .orElse(null);
        if (seller == null) return;
        // Create categories
        Category electronics = new Category();
        electronics.setName("Electronics");
        electronics.setDescription("Electronic devices and gadgets");
        electronics = categoryRepository.save(electronics);

        Category clothing = new Category();
        clothing.setName("Clothing");
        clothing.setDescription("Fashion and apparel");
        clothing = categoryRepository.save(clothing);

        // Create products
        Product phone = new Product();
        phone.setName("iPhone 15");
        phone.setPrice(BigDecimal.valueOf(79999.00));
        phone.setStockQuantity(50);
        phone.setImageUrl("https://imgs.search.brave.com/wr6kb_WiKIofQ5859rLsbEyLVEWwX6viwAM9ggDAJMc/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly93d3cu/YXBwbGUuY29tL25l/d3Nyb29tL2ltYWdl/cy8yMDIzLzA5L2Fw/cGxlLWRlYnV0cy1p/cGhvbmUtMTUtYW5k/LWlwaG9uZS0xNS1w/bHVzL2FydGljbGUv/QXBwbGUtaVBob25l/LTE1LWxpbmV1cC1o/ZXJvLWdlby0yMzA5/MTJfaW5saW5lLmpw/Zy5sYXJnZS5qcGc");
        phone.setDescription("Latest iPhone with advanced features");
        phone.setBrand("Apple");
        phone.setCategory(electronics);
        phone.setSeller(seller);
        phone.setDiscountPercent(10);
        productRepository.save(phone);

        Product laptop = new Product();
        laptop.setName("MacBook Pro");
        laptop.setPrice(BigDecimal.valueOf(129999.00));
        laptop.setStockQuantity(20);
        laptop.setImageUrl("https://imgs.search.brave.com/hyRaQLhxYdVhh703UFXao9vz2Ty1Ck_Ozmomg9_c7yE/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pcGxh/bmV0Lm9uZS9jZG4v/c2hvcC9maWxlcy9N/YWNCb29rX1Byb18x/NC1pbl9TcGFjZV9H/cmF5X1BEUF9JbWFn/ZV9Qb3NpdGlvbi0x/X19HQkVOXzYzMWRj/MzUzLTk2MDgtNGFh/ZC1hMjQwLTgxMjJl/NmQ4NDI1N18xNTAw/eC5qcGc_dj0xNjkx/MTQyOTIx");
        laptop.setDescription("Professional laptop for developers");
        laptop.setBrand("Apple");
        laptop.setCategory(electronics);
        laptop.setSeller(seller);
        laptop.setDiscountPercent(5);
        productRepository.save(laptop);

        Product shirt = new Product();
        shirt.setName("Cotton T-shirt");
        shirt.setPrice(BigDecimal.valueOf(999.0));
        shirt.setStockQuantity(100);
        shirt.setImageUrl("https://imgs.search.brave.com/ir2KlJlEUDro3eRUv6vHWyAlG2j_TQiHjdpQq6eramI/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9tLm1l/ZGlhLWFtYXpvbi5j/b20vaW1hZ2VzL0kv/NDFLdVVaZ0o3WEwu/anBn");
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