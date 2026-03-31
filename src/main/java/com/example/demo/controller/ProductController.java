package com.example.demo.controller;

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


		log.info("Creating product for user: {}", email);

		Product savedProduct = productService.create(product, email);

		return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
	}

	// ✅ GET PRODUCTS BY OWNER (PAGINATED)
	@GetMapping
	public ResponseEntity<Page<Product>> getProductsByOwner(@RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size,
			 @RequestParam(defaultValue = "asc") String sortDir) {

		String email = SecurityUtil.getCurrentUserEmail();
		int finalPage = (page != null) ? page : cartConfig.getDefaultPage();
		int finalSize = (size != null) ? size : cartConfig.getDefaultSize();

		log.info("Fetching products for user: {}", email);

	    Page<Product> products = productService.getProductsByOwner(email, finalPage, finalSize,sortDir);

		return ResponseEntity.ok(products);
	}

	// ✅ GET PRODUCT BY ID
	@GetMapping("/{id}")
	public ResponseEntity<Product> getById(@PathVariable("id") Integer id) {

		log.debug("Fetching product by id: {}", id);

		Product product = productService.getById(id);

		return ResponseEntity.ok(product);
	}

	// ✅ UPDATE PRODUCT (WITH OWNERSHIP CHECK)
	@PutMapping("/{id}")
	public ResponseEntity<Product> update(@PathVariable("id") Integer id, @Valid @RequestBody Product product) {

		String email = SecurityUtil.getCurrentUserEmail();

		log.info("Updating product id: {} by user: {}", id, email);

		Product updatedProduct = productService.update(id, product);

		return ResponseEntity.ok(updatedProduct);
	}

	// ✅ DELETE PRODUCT (WITH OWNERSHIP CHECK)

	// ✅ DELETE
	@DeleteMapping("/{id}")
	public ResponseEntity<?> delete(@PathVariable("id") Integer id) {

		log.info("Deleting product id: {}", id);

		boolean delete = productService.delete(id);
		return delete
		        ? ResponseEntity.status(HttpStatus.ACCEPTED).body(AppConstants.PRODUCT_DELETED)
		        : ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppConstants.PRODUCT_NOT_FOUND);
	}	
	
	@GetMapping("/getAllproducts")
	public ResponseEntity<?> getAllProducts(@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {

		Page<Product> products = productService.getAllProducts(page, size);
		return ResponseEntity.ok(products);
	}
	
	

}