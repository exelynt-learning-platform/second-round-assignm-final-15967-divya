package com.example.demo.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;
import com.example.demo.config.CartConfig;
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

	@Autowired
	private CartConfig cartConfig;

	@Override
	public Cart addToCart(String email, Integer productId, int quantity) {

		User user = getUserByEmail(email);
		Product product = getProductById(productId);

		validateQuantity(quantity);

		Cart existingCart = cartRepository.findByUserAndProduct(user, product);

		if (existingCart != null) {
			return updateExistingCart(existingCart, product, quantity);
		}

		validateStock(product, quantity);

		return createNewCart(user, product, quantity);
	}

	@Override
	public Cart updateCart(String email, Integer productId, int quantity) {

		User user = getUserByEmail(email);
		Product product = getProductById(productId);

		validateQuantity(quantity);
		validateStock(product, quantity);

		Cart cart = findCartByUserAndProductOrThrow(user, product);
		if (cart == null) {
			throw new CartException(AppConstants.CART_NOT_FOUND);
		}

		return updateExistingCart(cart, product, quantity);
	}

	@Override
	public List<Cart> getMyCart(String email) {
		User user = getUserByEmail(email);
		return cartRepository.findByUserId(user.getId());
	}

	@Override
	public boolean removeFromCart(String email, Integer productId) {

	    User user = getUserByEmail(email);
	    Product product = getProductById(productId);

	    Cart cart = findCartByUserAndProductOrThrow(user, product); // will throw if not found

	    cartRepository.delete(cart);
	    return true;
	}

	// ================= COMMON VALIDATION =================

	private void validateQuantity(int quantity) {

		if (quantity < cartConfig.getMinQuantity()) {
			throw new CartException(AppConstants.INVALID_QUANTITY);
		}

		if (quantity > cartConfig.getMaxQuantity()) {
			throw new CartException(AppConstants.MAX_QUANTITY_EXCEEDED + cartConfig.getMaxQuantity());
		}
	}

	private void validateStock(Product product, int quantity) {
		productValidationService.validateStock(product, quantity);
	}

	private Cart findCartByUserAndProductOrThrow(User user, Product product) {
		Cart cart = cartRepository.findByUserAndProduct(user, product);

		if (cart == null) {
			throw new CartException(AppConstants.CART_NOT_FOUND);
		}

		return cart;
	}

	// ================= BUSINESS METHODS =================

	private Cart updateExistingCart(Cart existingCart, Product product, int quantity) {

		int newQuantity = existingCart.getQuantity() + quantity;

		validateQuantity(newQuantity);

		validateStock(product, newQuantity);

		existingCart.setQuantity(newQuantity);
		existingCart.setPrice(product.getPrice());
		existingCart.setTotalPrice(calculateTotalPrice(product, newQuantity));

		return cartRepository.save(existingCart);
	}

	private Cart createNewCart(User user, Product product, int quantity) {

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

	    Product product = productRepository.findById(productId)
	            .orElseThrow(() -> new CartException(AppConstants.PRODUCT_NOT_FOUND + productId));

	    if (product.isDeleted()) {
	        throw new CartException(AppConstants.PRODUCT_NOT_FOUND_IN_CART);
	    }

	    return product;
	}
}