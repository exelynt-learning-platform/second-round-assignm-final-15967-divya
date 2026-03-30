package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Order;
import com.example.demo.Entity.Product;
import com.example.demo.constants.AppConstants;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	@Autowired
	private OrderService orderService;

	@PostMapping("/saveProduct")
	public ResponseEntity<Product> create(@Valid @RequestBody Product product) {

		String email = SecurityUtil.getCurrentUserEmail();

		log.info("Creating product for user: {}", email); // ✅ standardized

		Product savedProduct = productService.create(product, email);

		return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
	}

	@GetMapping("/getProductsByOwner")
	public ResponseEntity<Page<Product>> getProductsByOwner(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		String email = SecurityUtil.getCurrentUserEmail();

		log.info("Fetching products for user: {}", email); // ✅ consistent

		Page<Product> products = productService.getProductsByOwner(email, page, size);

		// ✅ removed null check
		if (products.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		return ResponseEntity.ok(products);
	}

	@GetMapping("/getProductById/{id}")
	public ResponseEntity<Product> getById(@PathVariable("id") Integer id) {

		log.debug("Fetching product by id: {}", id); // ✅ debug for internal

		Product product = productService.getById(id);

		return ResponseEntity.ok(product);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Product> update(@PathVariable("id") Integer id, @RequestBody Product product) {

		log.info("Updating product id: {}", id); // ✅ important action

		Product updated = productService.update(id, product);

		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/deleteById/{id}")
	public ResponseEntity<String> delete(@PathVariable("id") Integer id) {

		log.info("Deleting product id: {}", id); // ✅ important action

		boolean deleted = productService.delete(id);

		if (deleted) {
			return ResponseEntity.ok(AppConstants.PRODUCT_DELETED);
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppConstants.PRODUCT_NOT_FOUND);
	}

	@GetMapping("/getAllOrders")
	public ResponseEntity<List<Order>> allOrders() {

		log.info("Fetching all orders"); // ✅ added logging

		List<Order> orders = orderService.getAllOrders();

		// Optional: null check not needed if service guarantees non-null
		if (orders.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(orders);
	}
}