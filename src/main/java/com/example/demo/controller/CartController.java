package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Cart;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.CartService;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add/{productId}/{quantity}")
    public Cart addToCart(@PathVariable Integer productId,
                          @PathVariable int quantity) {
        return cartService.addToCart(SecurityUtil.getCurrentUserEmail(), productId, quantity);
    }

    @GetMapping("/my")
    public List<Cart> getMyCart() {
        return cartService.getMyCart(SecurityUtil.getCurrentUserEmail());
    }

    @PutMapping("/update/{productId}/{quantity}")
    public Cart updateCart(@PathVariable Integer productId,
                           @PathVariable int quantity) {
        return cartService.updateCart(SecurityUtil.getCurrentUserEmail(), productId, quantity);
    }

    @DeleteMapping("/removeProductFromCart/{productId}")
    public String remove(@PathVariable Integer productId) {
        return cartService.removeFromCart(SecurityUtil.getCurrentUserEmail(), productId)
                ? "Removed from cart"
                : "Failed to remove";
    }
}