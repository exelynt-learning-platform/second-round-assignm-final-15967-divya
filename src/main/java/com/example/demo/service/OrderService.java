package com.example.demo.service;

import java.util.List;

import com.example.demo.Entity.Order;

public interface OrderService {
	 Order placeOrder(String email, String shippingAddress);

	    List<Order> getMyOrders(String email);

	    List<Order> getAllOrders();
}
