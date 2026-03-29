package com.example.demo.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.Cart;
public interface CartRepository  extends JpaRepository<Cart, Integer> {

    List<Cart> findByUserId(Integer userId);

    Cart findByUserIdAndProductId(Integer userId, Integer productId);
}