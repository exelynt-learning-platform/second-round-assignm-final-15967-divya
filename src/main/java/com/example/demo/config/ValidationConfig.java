package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ValidationConfig {

    @Value("${product.stock.min:1}")
    private int minQuantity;

    @Value("${product.stock.max:1000}")
    private int maxQuantity;

    public int getMinQuantity() {
        return minQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }
}