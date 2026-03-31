package com.example.demo.service;

import org.springframework.data.domain.Page;

import com.example.demo.DTO.ProductRequestDTO;
import com.example.demo.Entity.Product;
public interface ProductService {

    Product create(ProductRequestDTO dto, String email);

    Page<Product> getProductsByOwner(int page, int size, String sortBy, String sortDir);

    Product getById(int id);

    Product update(Integer id, Product product);

    boolean delete(Integer id);

    Page<Product> getAllProducts(int page, int size, String sortBy, String sortDir);
}