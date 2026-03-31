package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.demo.Entity.Order;

public interface OrderService {
	Order placeOrder(String email, String shippingAddress);

	List<Order> getMyOrders(String email);

	Page<Order> getAllOrders(int page, int size, String sortBy, String sortDir);
}
