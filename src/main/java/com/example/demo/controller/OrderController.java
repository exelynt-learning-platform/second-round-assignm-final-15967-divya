package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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