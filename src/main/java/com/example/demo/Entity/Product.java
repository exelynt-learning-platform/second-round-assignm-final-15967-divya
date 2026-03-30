package com.example.demo.Entity;

import org.hibernate.annotations.SQLDelete;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products", uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "user_id" }) })
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@NotBlank(message = "Name is required")
	private String name;

	@NotBlank(message = "Description is required")
	private String description;

	@Positive(message = "Price must be greater than 0")
	private double price;
	@PositiveOrZero(message = "Stock cannot be negative")
	private int stockQuantity;
	@NotBlank(message = "Image URL is required")
	private String imageUrl;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	private boolean isDeleted = false;
}