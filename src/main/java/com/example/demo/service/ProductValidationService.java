package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.Entity.Product;
import com.example.demo.constants.AppConstants;

@Service
public class ProductValidationService {

    public void validateStock(Product product, int quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException(
                AppConstants.INSUFFICIENT_STOCK + product.getName()
            );
        }
    }
}