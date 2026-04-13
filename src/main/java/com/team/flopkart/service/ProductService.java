package com.team.flopkart.service;

import com.team.flopkart.model.Product;
import com.team.flopkart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Returns all available products (placeholder implementation).
     * If no products exist yet, seeds a few placeholders to support testing.
     */
    public List<Product> getAllProducts() {
        List<Product> allProducts = productRepository.findAll();

        if (allProducts.isEmpty()) {
            return createDemoProducts();
        }

        return allProducts;
    }

    private List<Product> createDemoProducts() {
        List<Product> demoProducts = new ArrayList<>();

        Product p1 = new Product();
        p1.setName("Wireless Earbuds");
        p1.setDescription("Premium wireless earbuds with noise cancellation");
        p1.setPrice(79.99);
        p1.setBrand("TechBrand");
        p1.setStockQuantity(50);
        p1.setActive(true);
        demoProducts.add(productRepository.save(p1));

        Product p2 = new Product();
        p2.setName("USB-C Cable");
        p2.setDescription("Durable USB-C charging cable, 2 meters");
        p2.setPrice(12.99);
        p2.setBrand("CableCorp");
        p2.setStockQuantity(100);
        p2.setActive(true);
        demoProducts.add(productRepository.save(p2));

        Product p3 = new Product();
        p3.setName("Phone Screen Protector");
        p3.setDescription("Tempered glass screen protector for smartphones");
        p3.setPrice(9.99);
        p3.setBrand("ProtectMax");
        p3.setStockQuantity(75);
        p3.setActive(true);
        demoProducts.add(productRepository.save(p3));

        Product p4 = new Product();
        p4.setName("Laptop Stand");
        p4.setDescription("Adjustable aluminum laptop stand for better ergonomics");
        p4.setPrice(34.99);
        p4.setBrand("ErgoTech");
        p4.setStockQuantity(30);
        p4.setActive(true);
        demoProducts.add(productRepository.save(p4));

        return demoProducts;
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
}
