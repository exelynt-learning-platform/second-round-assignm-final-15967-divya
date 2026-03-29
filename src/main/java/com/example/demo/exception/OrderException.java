package com.example.demo.exception;

public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }
}