package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class CartConfig {

    @Value("${cart.min.quantity}")
    private int minQuantity;

    @Value("${cart.max.quantity}")
    private int maxQuantity;

    @Value("${pagination.default.page}")
    private int defaultPage;

    @Value("${pagination.default.size}")
    private int defaultSize;

    @Value("${address.min.length}")
    private int minAddressLength;

    @Value("${address.max.length}")
    private int maxAddressLength;

    public int getMinQuantity() {
        return minQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public int getDefaultPage() {
        return defaultPage;
    }

    public int getDefaultSize() {
        return defaultSize;
    }

    public int getMinAddressLength() {
        return minAddressLength;
    }

    public int getMaxAddressLength() {
        return maxAddressLength;
    }
}