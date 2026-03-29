package com.example.demo.controller;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Product;
import com.example.demo.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;

	// Delete
	@RequestMapping("/hii")
	public void hiiii() {
		System.out.println("hiiii  admin");
	}

	@PostMapping("/saveProduct")
	public ResponseEntity<?> create(@RequestBody Product product) {
		log.debug("save product {} ", product);
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();
		Product savedProduct = productService.create(product, email);
		System.out.println(savedProduct + "   savedProduct");

		return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
	}

	@GetMapping("/getProductsByOwner")
	public ResponseEntity<?> getProductsByOwner(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String email = auth.getName();

		log.info("Fetching products for user: {}", email);

		Page<Product> products = productService.getProductsByOwner(email, page, size);

		if (products.isEmpty()) {
			throw new RuntimeException("No products found for this user");
		}
		return ResponseEntity.ok(products);
	}

	@GetMapping("/getProductById/{id}")
	public ResponseEntity<Product> getById(@PathVariable("id") Integer id) {

		Product product = productService.getById(id);

		return ResponseEntity.ok(product);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Product> update(@PathVariable("id") Integer id, @RequestBody Product product) {
		Product update = productService.update(id, product);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(update);
	}

	@DeleteMapping("/deleteProductById/{id}")
	public ResponseEntity<?> delete(@PathVariable("id") Integer id) {

		boolean deleted = productService.delete(id);

		if (deleted) {
			return ResponseEntity.ok("Product deleted successfully");
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
	}

}
