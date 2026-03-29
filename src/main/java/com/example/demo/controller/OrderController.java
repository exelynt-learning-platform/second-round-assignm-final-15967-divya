package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entity.Order;
import com.example.demo.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @PostMapping("/place")
    @PreAuthorize("hasRole('USER')")
    public Order placeOrder(@RequestParam String shippingAddress) {
        return orderService.placeOrder(getEmail(), shippingAddress);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public List<Order> myOrders() {
        return orderService.getMyOrders(getEmail());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")   // ✅ FIX
    public List<Order> allOrders() {
        return orderService.getAllOrders();
    }
}