package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Entity.Product;
import com.example.demo.constants.AppConstants;
import com.example.demo.exception.ProductNotFoundException;
import com.example.demo.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/userproducts")
public class UserController {

	@Autowired
	private ProductService productService;

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