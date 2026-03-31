package com.example.demo.enums;

import java.util.Arrays;
import com.example.demo.exception.ProductException;

public enum OrderSortField {

    ID("id"),
    TOTAL_PRICE("totalPrice"),
    SHIPPING_ADDRESS("shippingAddress"),
    PAYMENT_STATUS("paymentStatus"),
    CREATED_DATE("createdDate");

    private final String field;

    OrderSortField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public static String from(String value) {
        return Arrays.stream(values())
                .filter(f -> f.field.equalsIgnoreCase(value))
                .map(OrderSortField::getField)
                .findFirst()
                .orElseThrow(() ->
                        new ProductException("Invalid order sort field: " + value)
                );
    }
}