package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUserId(Integer userId);
}