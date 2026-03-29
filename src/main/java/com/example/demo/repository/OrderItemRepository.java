package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
	List<OrderItem> findByOrderId(Integer orderId);
}