package com.example.demo.service;

import java.util.List;

import com.example.demo.Entity.Cart;

public interface CartService {

    Cart addToCart(String email, Integer productId, int quantity);

    List<Cart> getMyCart(String email);

    Cart updateCart(String email, Integer productId, int quantity);

    boolean removeFromCart(String email, Integer productId);
}
