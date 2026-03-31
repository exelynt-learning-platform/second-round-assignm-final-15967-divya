package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public Cart addToCart(@PathVariable Integer productId, @PathVariable int quantity) {
		return cartService.addToCart(SecurityUtil.getCurrentUserEmail(), productId, quantity);
	}

	@GetMapping("/my")
	public List<Cart> getMyCart() {
		return cartService.getMyCart(SecurityUtil.getCurrentUserEmail());
	}

	@PutMapping("/{productId}/{quantity}")
	public Cart updateCart(@PathVariable Integer productId, @PathVariable int quantity) {
		return cartService.updateCart(SecurityUtil.getCurrentUserEmail(), productId, quantity);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<?> remove(@PathVariable Integer productId) {

		boolean isRemoved = cartService.removeFromCart(SecurityUtil.getCurrentUserEmail(), productId);
		return isRemoved ? ResponseEntity.status(HttpStatus.ACCEPTED).body(AppConstants.PRODUCT_REMOVED_FROM_CART)
				: ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppConstants.PRODUCT_NOT_FOUND_IN_CART);
	}
}