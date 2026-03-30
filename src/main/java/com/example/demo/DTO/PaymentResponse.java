package com.example.demo.DTO;

public class PaymentResponse {

    private String message;
    private String url;

    public PaymentResponse() {
    }

    public PaymentResponse(String message, String url) {
        this.message = message;
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}