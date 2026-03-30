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

    private static final String USER_NOT_FOUND = "User not found";
    private static final String PRODUCT_NOT_FOUND = "Product not found with id: ";
    private static final String CART_NOT_FOUND = "Cart item not found";
    private static final String INSUFFICIENT_STOCK = "Insufficient stock. Available: ";

    @Override
    public Cart addToCart(String email, Integer productId, int quantity) {

        User user = getUserByEmail(email);
        Product product = getProductById(productId);

        Cart existingCart = cartRepository.findByUserAndProduct(user, product);

        if (existingCart != null) {
            return updateExistingCart(existingCart, product, quantity);
        }

        return createNewCart(user, product, quantity);
    }

    @Override
    public List<Cart> getMyCart(String email) {
        User user = getUserByEmail(email);
        return cartRepository.findByUser(user);
    }

    @Override
    public Cart updateCart(String email, Integer productId, int quantity) {

        User user = getUserByEmail(email);
        Product product = getProductById(productId);

        validateStock(product, quantity);

        Cart cart = cartRepository.findByUserAndProduct(user, product);

        if (cart == null) {
            throw new CartException(CART_NOT_FOUND);
        }

        cart.setQuantity(quantity);
        cart.setPrice(product.getPrice());
        cart.setTotalPrice(calculateTotalPrice(product, quantity));

        return cartRepository.save(cart);
    }

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

    // ================= PRIVATE METHODS =================

    private Cart updateExistingCart(Cart existingCart, Product product, int quantity) {

        int newQuantity = existingCart.getQuantity() + quantity;

        validateStock(product, newQuantity);

        existingCart.setQuantity(newQuantity);
        existingCart.setTotalPrice(calculateTotalPrice(product, newQuantity));

        return cartRepository.save(existingCart);
    }

    private Cart createNewCart(User user, Product product, int quantity) {

        validateStock(product, quantity);

        Cart cart = new Cart();
        cart.setUser(user);
        cart.setProduct(product);
        cart.setQuantity(quantity);
        cart.setPrice(product.getPrice());
        cart.setProductName(product.getName());
        cart.setTotalPrice(calculateTotalPrice(product, quantity));

        return cartRepository.save(cart);
    }

    private double calculateTotalPrice(Product product, int quantity) {
        return quantity * product.getPrice();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CartException(USER_NOT_FOUND));
    }

    private Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CartException(PRODUCT_NOT_FOUND + productId));
    }

    private void validateStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new CartException(INSUFFICIENT_STOCK + product.getStockQuantity());
        }
    }
}