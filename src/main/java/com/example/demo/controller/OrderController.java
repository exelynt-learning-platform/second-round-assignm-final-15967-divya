package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Order;
import com.example.demo.Entity.User;
import com.example.demo.exception.OrderException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OrderService;

import jakarta.validation.constraints.NotBlank;
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    private String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @PostMapping("/place")
    public Order placeOrder(@RequestParam("shippingAddress") @NotBlank String shippingAddress) {
        return orderService.placeOrder(getEmail(), shippingAddress);
    }

    @GetMapping("/my")
    public List<Order> myOrders() {
        return orderService.getMyOrders(getEmail());
    }

    @GetMapping("/all")
    public List<Order> allOrders() {

        User user = userRepository.findByEmail(getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("Access Denied");
        }

        return orderService.getAllOrders();
    }
}