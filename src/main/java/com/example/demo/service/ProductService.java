package com.example.demo.service;

import org.springframework.data.domain.Page;

import com.example.demo.Entity.Product;

public interface ProductService {

	Product create(Product product, String email);

	Page<Product> getProductsByOwner(String email, int page, int size,String sortBy,String sortDir);

	Product getById(int id);

	Product update(Integer id, Product product);

	boolean delete(Integer id);

	public Page<Product> getAllProducts(int page, int size, String sortBy, String sortDir);
	}
