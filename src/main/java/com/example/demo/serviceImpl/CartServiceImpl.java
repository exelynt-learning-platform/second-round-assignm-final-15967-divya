package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.exception.CartException;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Cart addToCart(String email, Integer productId, int quantity) {

        User user = getUserByEmail(email);
        Product product = getProductById(productId);

        // 🔥 IMPORTANT: Update your repository accordingly
        Cart existing = cartRepository.findByUserAndProduct(user, product);

        if (existing != null) {

            int newQuantity = existing.getQuantity() + quantity;

            validateStock(product, newQuantity);

            existing.setQuantity(newQuantity);
            existing.setTotalPrice(newQuantity * product.getPrice());

            return cartRepository.save(existing);
        }

        // New Cart Item
        validateStock(product, quantity);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProduct(product);
        cart.setQuantity(quantity);
        cart.setPrice(product.getPrice());
        cart.setProductName(product.getName());
        cart.setTotalPrice(quantity * product.getPrice());

        return cartRepository.save(cart);
    }

    // ✅ Get My Cart
    @Override
    public List<Cart> getMyCart(String email) {
        User user = getUserByEmail(email);
        return cartRepository.findByUser(user);
    }

    // ✅ Update Cart
    @Override
    public Cart updateCart(String email, Integer productId, int quantity) {

        User user = getUserByEmail(email);
        Product product = getProductById(productId);

        Cart cart = cartRepository.findByUserAndProduct(user, product);

        if (cart == null) {
            throw new CartException("Cart item not found");
        }

        validateStock(product, quantity);

        cart.setQuantity(quantity);
        cart.setPrice(product.getPrice());
        cart.setTotalPrice(quantity * product.getPrice());

        return cartRepository.save(cart);
    }

    // ✅ Remove from Cart
    @Override
    public boolean removeFromCart(String email, Integer productId) {

        User user = getUserByEmail(email);
        Product product = getProductById(productId);

        Cart cart = cartRepository.findByUserAndProduct(user, product);

        if (cart == null) {
            return false;
        }

        cartRepository.delete(cart);
        return true;
    }

    // ================= HELPER METHODS =================

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CartException("User not found"));
    }

    private Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CartException("Product not found with id: " + productId));
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new CartException("Insufficient stock. Available: " + product.getStockQuantity());
        }
    }
}