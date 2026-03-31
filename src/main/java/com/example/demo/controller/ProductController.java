package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Entity.Product;
import com.example.demo.config.CartConfig;
import com.example.demo.constants.AppConstants;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.security.SecurityUtil;
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
	private CartConfig cartConfig;

	// ✅ CREATE PRODUCT
	@PostMapping
	public ResponseEntity<Product> create(@Valid @RequestBody Product product) {

		String email = SecurityUtil.getCurrentUserEmail();

		if (product == null) {
			throw new IllegalArgumentException(AppConstants.INVALID_PRODUCT);
		}

		log.info("Creating product for user: {}", email);

		Product savedProduct = productService.create(product, email);

		return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
	}

	// ✅ GET PRODUCTS BY OWNER (PAGINATED)
	@GetMapping
	public ResponseEntity<Page<Product>> getProductsByOwner(@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {

		String email = SecurityUtil.getCurrentUserEmail();
		int finalPage = (page != null) ? page : cartConfig.getDefaultPage();
		int finalSize = (size != null) ? size : cartConfig.getDefaultSize();

		log.info("Fetching products for user: {}", email);

	    Page<Product> products = productService.getProductsByOwner(email, finalPage, finalSize);

		return ResponseEntity.ok(products);
	}

	// ✅ GET PRODUCT BY ID
	@GetMapping("/{id}")
	public ResponseEntity<Product> getById(@PathVariable("id") Integer id) {

		if (id == null || id <= 0) {
			throw new IllegalArgumentException(AppConstants.INVALID_ID);
		}

		log.debug("Fetching product by id: {}", id);

		Product product = productService.getById(id);

		return ResponseEntity.ok(product);
	}

	// ✅ UPDATE PRODUCT (WITH OWNERSHIP CHECK)
	@PutMapping("/{id}")
	public ResponseEntity<Product> update(@PathVariable("id") Integer id, @Valid @RequestBody Product product) {

		String email = SecurityUtil.getCurrentUserEmail();

		if (id == null || id <= 0) {
			throw new IllegalArgumentException(AppConstants.INVALID_ID);
		}

		if (product == null) {
			throw new IllegalArgumentException(AppConstants.INVALID_PRODUCT);
		}

		log.info("Updating product id: {} by user: {}", id, email);

		Product updatedProduct = productService.update(id, product);

		return ResponseEntity.ok(updatedProduct);
	}

	// ✅ DELETE PRODUCT (WITH OWNERSHIP CHECK)

	// ✅ DELETE
	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable("id") Integer id) {

		log.info("Deleting product id: {}", id);

		productService.delete(id);

		return ResponseEntity.ok(AppConstants.PRODUCT_DELETED);
	}	
	
	@GetMapping("/getAllproducts")
	public ResponseEntity<?> getAllProducts(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		Page<Product> products = productService.getAllProducts(page, size);

		if (products.isEmpty()) {
			throw new ProductNotFoundException(AppConstants.PRODUCT_NOT_FOUND);
		}

		return ResponseEntity.ok(products);
	}
	
	

}