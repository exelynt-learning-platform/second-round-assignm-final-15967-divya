package com.example.demo.Entity;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer orderId;

    private Integer productId;

    private String productName;

    private int quantity;

    private double price;
}