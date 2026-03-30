package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.constants.AppConstants;
import com.example.demo.exception.CartException;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.ProductValidationService;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ProductValidationService productValidationService;

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
		return cartRepository.findByUserId(user.getId());
	}

	@Override
	public Cart updateCart(String email, Integer productId, int quantity) {

		User user = getUserByEmail(email);
		Product product = getProductById(productId);

		productValidationService.validateStock(product, quantity);
		Cart cart = cartRepository.findByUserAndProduct(user, product);

		if (cart == null) {
			throw new CartException(AppConstants.CART_NOT_FOUND);
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

	    if (newQuantity > AppConstants.MAX_CART_QUANTITY) {
	        throw new CartException(
	            AppConstants.MAX_CART_QUANTITY_EXCEEDED + AppConstants.MAX_CART_QUANTITY
	        );
	    }

	    if (newQuantity > product.getStockQuantity()) {
	        throw new CartException(AppConstants.INSUFFICIENT_STOCK);
	    }

	    existingCart.setQuantity(newQuantity);
	    existingCart.setTotalPrice(calculateTotalPrice(product, newQuantity));

	    return cartRepository.save(existingCart);
	}
	private Cart createNewCart(User user, Product product, int quantity) {

		productValidationService.validateStock(product, quantity);
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
		return userRepository.findByEmail(email).orElseThrow(() -> new CartException(AppConstants.USER_NOT_FOUND));
	}

	private Product getProductById(Integer productId) {
		return productRepository.findById(productId)
				.orElseThrow(() -> new CartException(AppConstants.PRODUCT_NOT_FOUND + productId));
	}


}