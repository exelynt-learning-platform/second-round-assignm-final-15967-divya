package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
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

	public Cart addToCart(String email, Integer productId, int quantity) {

	    User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    Product product = productRepository.findById(productId)
	            .orElseThrow(() -> new RuntimeException("Product not found"));

	    Cart existing = cartRepository.findByUserIdAndProductId(user.getId(), productId);

	    if (existing != null) {

	        int newQuantity = existing.getQuantity() + quantity;

	        if (product.getStockQuantity() < newQuantity) {
	            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
	        }

	        existing.setQuantity(newQuantity);
	        existing.setTotalPrice(newQuantity * product.getPrice());

	        return cartRepository.save(existing);

	    } else {

	        if (product.getStockQuantity() < quantity) {
	            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
	        }

	        Cart cart = new Cart();
	        cart.setUserId(user.getId());
	        cart.setProductId(productId);
	        cart.setQuantity(quantity);
	        cart.setTotalPrice(quantity * product.getPrice());
	        cart.setProductName(product.getName());

	        return cartRepository.save(cart);
	    }
	}
	
	@Override
	public List<Cart> getMyCart(String email) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		return cartRepository.findByUserId(user.getId());
	}

	@Override
	public Cart updateCart(String email, Integer productId, int quantity) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		Cart cart = cartRepository.findByUserIdAndProductId(user.getId(), productId);

		if (cart == null) {
			throw new RuntimeException("Cart item not found");
		}

		cart.setQuantity(quantity);
		cart.setTotalPrice(quantity * cart.getPrice());

		return cartRepository.save(cart);
	}

	@Override
	public boolean removeFromCart(String email, Integer productId) {

		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

		Cart cart = cartRepository.findByUserIdAndProductId(user.getId(), productId);

		if (cart == null) {
			return false;
		}

		cartRepository.delete(cart);
		return true;
	}
}