package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.DTO.PaymentResponse;
import com.example.demo.service.PaymentService;

import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payment")
@Validated
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create/{orderId}")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable("orderId") 
            @Min(value = 1, message = "OrderId must be greater than 0") Integer orderId)
            throws Exception {

        log.info("Payment initiation started for orderId: {}", orderId);

        String url = paymentService.createPaymentSession(orderId);

        log.debug("Payment session URL generated for orderId {}: {}", orderId, url);

        PaymentResponse response = new PaymentResponse(
                "Payment session created successfully",
                url
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/success/{orderId}")
    public ResponseEntity<PaymentResponse> paymentSuccess(
            @PathVariable 
            @Min(value = 1, message = "Invalid orderId") Integer orderId) {

        log.info("Payment success callback received for orderId: {}", orderId);

        paymentService.success(orderId);

        return ResponseEntity.ok(
                new PaymentResponse("Payment successful", null)
        );
    }

    @GetMapping("/failure/{orderId}")
    public ResponseEntity<PaymentResponse> paymentFailure(
            @PathVariable 
            @Min(value = 1, message = "Invalid orderId") Integer orderId) {

        log.warn("Payment failure callback received for orderId: {}", orderId);

        paymentService.failure(orderId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new PaymentResponse("Payment failed", null)
        );
    }
}