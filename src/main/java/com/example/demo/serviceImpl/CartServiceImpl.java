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

	    // ✅ Get user
	    User user = getUserByEmail(email);

	    // ✅ Get product
	    Product product = getProductById(productId);

	    // ❗ Validate quantity (using constants)
	    if (quantity <= 0) {
	        throw new CartException(AppConstants.INVALID_QUANTITY);
	    }

	    if (quantity > AppConstants.MAX_CART_QUANTITY) {
	        throw new CartException(
	            AppConstants.MAX_QUANTITY_EXCEEDED + AppConstants.MAX_CART_QUANTITY
	        );
	    }

	    // ❗ Validate stock
	    productValidationService.validateStock(product, quantity);

	    // ✅ Fetch existing cart
	    Cart cart = cartRepository.findByUserAndProduct(user, product);

	    if (cart == null) {
	        throw new CartException(AppConstants.CART_NOT_FOUND);
	    }

	    // ✅ Reuse existing logic (updateExistingCart)
	    return updateExistingCart(cart, product, quantity);
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

	    // ✅ Null safety check
	    if (existingCart.getProduct() == null) {
	        throw new CartException(AppConstants.PRODUCT_NOT_FOUND);
	    }

	    Product cartProduct = existingCart.getProduct();

	    int newQuantity = existingCart.getQuantity() + quantity;

	    // ❗ Validate max cart quantity
	    if (newQuantity > AppConstants.MAX_CART_QUANTITY) {
	        throw new CartException(
	            AppConstants.MAX_QUANTITY_EXCEEDED + AppConstants.MAX_CART_QUANTITY
	        );
	    }

	    // ❗ Validate stock using latest product data
	    productValidationService.validateStock(cartProduct, newQuantity);

	    // ❗ Recalculate using latest product info
	    existingCart.setQuantity(newQuantity);
	    existingCart.setPrice(cartProduct.getPrice());
	    existingCart.setTotalPrice(calculateTotalPrice(cartProduct, newQuantity));

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