package com.example.demo.Entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "orders") 
@Entity
@Getter@Setter@NoArgsConstructor@AllArgsConstructor
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