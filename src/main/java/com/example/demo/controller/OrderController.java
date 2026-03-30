package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entity.Order;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.OrderService;

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

}