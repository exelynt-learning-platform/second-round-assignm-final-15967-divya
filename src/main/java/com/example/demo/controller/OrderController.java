package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Order;
import com.example.demo.constants.AppConstants;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping("/place")
	public Order placeOrder(@RequestParam String shippingAddress) {
		return orderService.placeOrder(SecurityUtil.getCurrentUserEmail(), shippingAddress);
	}

	@GetMapping("/my")
	public List<Order> myOrders() {
		return orderService.getMyOrders(SecurityUtil.getCurrentUserEmail());
	}


	@GetMapping("/all")
	public ResponseEntity<?> allOrders(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestParam(defaultValue = "id") String sortBy,
	        @RequestParam(defaultValue = "desc") String sortDir) {

	    log.info("Fetching all orders with pagination");

	    Page<Order> orders = orderService.getAllOrders(page, size, sortBy, sortDir);

	    if (orders.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(AppConstants.NO_ORDERS_FOUND);
	    }

	    return ResponseEntity.ok(orders);
	}
}