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

	// ✅ CREATE
	@PostMapping
	public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
		String email = SecurityUtil.getCurrentUserEmail();
		log.info("Creating product for user: {}", email);

		Product savedProduct = productService.create(product, email);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
	}

	// ✅ GET PRODUCTS BY OWNER (no custom path)
	@GetMapping
	public ResponseEntity<Page<Product>> getProductsByOwner(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		String email = SecurityUtil.getCurrentUserEmail();
		log.info("Fetching products for user: {}", email);

		Page<Product> products = productService.getProductsByOwner(email, page, size);

		return ResponseEntity.ok(products); // ✅ removed redundant 404
	}

	// ✅ GET BY ID
	@GetMapping("/{id}")
	public ResponseEntity<Product> getById(@PathVariable("id") Integer id) {
		log.debug("Fetching product by id: {}", id);

		Product product = productService.getById(id);
		return ResponseEntity.ok(product);
	}

	// ✅ UPDATE
	@PutMapping("/{id}")
	public ResponseEntity<Product> update(@PathVariable("id") Integer id, @RequestBody Product product) {

		log.info("Updating product id: {}", id);

		Product updated = productService.update(id, product);
		return ResponseEntity.ok(updated);
	}

	// ✅ DELETE
	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable("id") Integer id) {

		log.info("Deleting product id: {}", id);

		productService.delete(id);

		return ResponseEntity.ok(AppConstants.PRODUCT_DELETED);
	}


}