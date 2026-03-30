package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.Cart;
import com.example.demo.Entity.Product;
import com.example.demo.Entity.User;

public interface CartRepository extends JpaRepository<Cart, Integer> {

	Cart findByUserAndProduct(User user, Product product);

	List<Cart> findByUserId(Integer user);

}