package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer userId;

	private Integer productId;

	private String productName;

	private double price;

	private int quantity;

	private double totalPrice;
}