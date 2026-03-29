package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.demo.Entity.Product;

public interface ProductService {

	Product create(Product product,String email);

	Page<Product> getProductsByOwner(String email, int page, int size);
	Product getById(int id);

	Product update(Integer id, Product product);

	String delete(Integer id);
}
