package com.team.flopkart.service;

import com.team.flopkart.model.Product;
import com.team.flopkart.model.Seller;
import com.team.flopkart.repository.CategoryRepository;
import com.team.flopkart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.team.flopkart.model.Category;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    public List<Product> getProductsBySeller(Seller seller) {
        return productRepository.findBySeller(seller);
    }

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
        p1.setPrice(BigDecimal.valueOf(79.88));
        p1.setBrand("TechBrand");
        p1.setStockQuantity(50);
        p1.setActive(true);
        demoProducts.add(productRepository.save(p1));

        Product p2 = new Product();
        p2.setName("USB-C Cable");
        p2.setDescription("Durable USB-C charging cable, 2 meters");
        p2.setPrice(BigDecimal.valueOf(12.34));
        p2.setBrand("CableCorp");
        p2.setStockQuantity(100);
        p2.setActive(true);
        demoProducts.add(productRepository.save(p2));

        Product p3 = new Product();
        p3.setName("Phone Screen Protector");
        p3.setDescription("Tempered glass screen protector for smartphones");
        p3.setPrice(BigDecimal.valueOf(9.99));
        p3.setBrand("ProtectMax");
        p3.setStockQuantity(75);
        p3.setActive(true);
        demoProducts.add(productRepository.save(p3));

        Product p4 = new Product();
        p4.setName("Laptop Stand");
        p4.setDescription("Adjustable aluminum laptop stand for better ergonomics");
        p4.setPrice(BigDecimal.valueOf(5.66));
        p4.setBrand("ErgoTech");
        p4.setStockQuantity(30);
        p4.setActive(true);
        demoProducts.add(productRepository.save(p4));

        return demoProducts;
    }

    public Optional<Product> getProductById(Long productId) {
        return productRepository.findById(productId);
    }

    public Product createProduct(Product product, Seller seller, Long categoryId) {
        // Set seller
        product.setSeller(seller);
        
        // Set category
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }
        
        // Defaults
        if (product.getActive() == null) {
            product.setActive(true);
        }
        if (product.getDiscountPercent() == null) {
            product.setDiscountPercent(0);
        }
        if (product.getStockQuantity() == null) {
            product.setStockQuantity(0);
        }
        
        return productRepository.save(product);
    }
    public Product updateProduct(Long productId, Product updatedProduct, Seller seller, Long categoryId) {
        Product existingProduct = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        // Security check - only owner can update
        if (!existingProduct.getSeller().getId().equals(seller.getId())) {
            throw new IllegalStateException("You can only edit your own products");
        }
        
        // Update fields
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDiscountPercent(updatedProduct.getDiscountPercent());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setActive(updatedProduct.getActive());
        
        // Update category if changed
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            existingProduct.setCategory(category);
        }
        
        return productRepository.save(existingProduct);
    }
    public void deleteProduct(Long productId, Seller seller) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        // Security check
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalStateException("You can only delete your own products");
        }
        
        // Soft delete
        product.setActive(false);
        productRepository.save(product);
    }
    public void toggleActiveStatus(Long productId, Seller seller) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        
        // Security check
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new IllegalStateException("You can only modify your own products");
        }
        
        product.setActive(!product.getActive());
        productRepository.save(product);
    }
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
