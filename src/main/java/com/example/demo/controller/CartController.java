package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Cart;
import com.example.demo.service.CartService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @PostMapping("/add/{productId}/{quantity}")
    public Cart addToCart( @PathVariable("productId") Integer productId,
            @PathVariable("quantity") int quantity) {

        return cartService.addToCart(getEmail(), productId, quantity);
    }

    @GetMapping("/my")
    public List<Cart> getMyCart() {
        return cartService.getMyCart(getEmail());
    }

    @PutMapping("/update/{productId}/{quantity}")
    public Cart updateCart(@PathVariable("productId") Integer productId,
                           @PathVariable("quantity") int quantity) {

        return cartService.updateCart(getEmail(), productId, quantity);
    }

    @DeleteMapping("/removeProductFromCart/{productId}")
    public String remove(@PathVariable("productId") Integer productId) {

        boolean removed = cartService.removeFromCart(getEmail(), productId);

        if (removed) {
            return "Removed from cart";
        }

        return "Failed to remove";
    }
}