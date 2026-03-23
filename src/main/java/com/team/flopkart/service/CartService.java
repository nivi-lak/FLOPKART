// src/main/java/com/team/flopkart/service/CartService.java
package com.team.flopkart.service;

import com.team.flopkart.model.Cart;
import com.team.flopkart.model.CartItem;
import com.team.flopkart.model.Product;
import com.team.flopkart.model.User;
import com.team.flopkart.pattern.singleton.CartSessionManager;
import com.team.flopkart.repository.CartItemRepository;
import com.team.flopkart.repository.CartRepository;
import com.team.flopkart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Concrete implementation of ICartService.
 * Annotated with @Service so Spring manages it as a bean.
 *
 * @Transactional on methods means: if anything throws an exception
 * mid-way, the entire DB operation rolls back. No half-saved data.
 */
@Service
public class CartService implements ICartService {

    // Constructor injection — NOT @Autowired on fields
    private final CartSessionManager cartSessionManager;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                   CartItemRepository cartItemRepository,
                   ProductRepository productRepository,
                   CartSessionManager cartSessionManager) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartSessionManager = cartSessionManager;
    }

    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cart getOrCreateCart(User user) {
        // If user already has a cart, return it. Otherwise make a new one.
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cart addToCart(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // Stock validation — never add more than available
        if (quantity > product.getStockQuantity()) {
            throw new RuntimeException("Only " + product.getStockQuantity() + " units in stock.");
        }

        // Check if this product is already in the cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            // Just bump the quantity
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + quantity;
            if (newQty > product.getStockQuantity()) {
                throw new RuntimeException("Cannot add more than available stock.");
            }
            item.setQuantity(newQty);
            cartItemRepository.save(item);
        } else {
            // Create a fresh CartItem
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
            cart.getItems().add(newItem);
        }

        Cart saved = cartRepository.save(cart);
        syncSession(saved);       
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cart updateQuantity(User user, Long cartItemId, int newQuantity) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Security check: make sure this item actually belongs to this user's cart
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (newQuantity <= 0) {
            // Treat quantity of 0 as "remove this item"
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            if (newQuantity > item.getProduct().getStockQuantity()) {
                throw new RuntimeException("Not enough stock.");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        }

        Cart saved = cartRepository.save(cart);
        syncSession(saved);       
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Cart removeItem(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        Cart saved = cartRepository.save(cart);
        syncSession(saved);       
        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear(); // orphanRemoval=true on Cart will delete all items
        cartRepository.save(cart);
        cartSessionManager.clearSession(user.getId()); 
    }
    
    private void syncSession(Cart cart) {
    cartSessionManager.updateSession(
        cart.getUser().getId(),
        cart.getItems().size()
    );
    }
}

// CartService.java
// Implements all cart logic:
// add item
// update quantity
// remove item
// validate stock

// OOAD concept: Service Layer Pattern + Encapsulation of Business Logic