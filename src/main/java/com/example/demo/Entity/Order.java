package com.example.demo.Entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Table(name = "orders") 
@Entity
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    private double totalPrice;

    private String shippingAddress;

    private String paymentStatus; // PENDING, PAID, FAILED

    private String status; // CREATED, SHIPPED, DELIVERED

    private Date createdDate;
}