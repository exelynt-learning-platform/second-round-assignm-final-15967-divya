package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Cart;
import com.example.demo.constants.AppConstants;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private CartService cartService;

	@PostMapping("/add/{productId}/{quantity}")
	public ResponseEntity<Cart> addToCart(@PathVariable Integer productId, @PathVariable int quantity) {

		Cart cart = cartService.addToCart(SecurityUtil.getCurrentUserEmail(), productId, quantity);

		return ResponseEntity.status(HttpStatus.CREATED).body(cart);
	}

	@GetMapping("/my")
	public ResponseEntity<List<Cart>> getMyCart() {

		List<Cart> cartList = cartService.getMyCart(SecurityUtil.getCurrentUserEmail());

		return ResponseEntity.ok(cartList);
	}

	@PutMapping("/{productId}/{quantity}")
	public ResponseEntity<Cart> updateCart(@PathVariable Integer productId, @PathVariable int quantity) {

		Cart updatedCart = cartService.updateCart(SecurityUtil.getCurrentUserEmail(), productId, quantity);

		return ResponseEntity.ok(updatedCart);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<String> remove(@PathVariable Integer productId) {

		boolean isRemoved = cartService.removeFromCart(SecurityUtil.getCurrentUserEmail(), productId);

		return isRemoved ? ResponseEntity.ok(AppConstants.CART_REMOVED_SUCCESS)
				: ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppConstants.PRODUCT_NOT_FOUND_IN_CART);

	}
}