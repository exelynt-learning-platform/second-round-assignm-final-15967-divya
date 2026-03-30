package com.example.demo.controller;

import com.example.demo.DTO.PaymentResponse;
import com.example.demo.service.PaymentService;

import jakarta.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@Validated
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create/{orderId}")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable("orderId") @Min(value = 1, message = "OrderId must be greater than 0") Integer orderId)
            throws Exception {

    	System.out.println("hi payment started");
        String url = paymentService.createPaymentSession(orderId);
    	System.out.println("hi payment started url-> "+url  );

        PaymentResponse response = new PaymentResponse(
                "Payment session created successfully",
                url
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/success/{orderId}")
    public ResponseEntity<PaymentResponse> paymentSuccess(
            @PathVariable @Min(value = 1, message = "Invalid orderId") Integer orderId) {

        paymentService.success(orderId);

        return ResponseEntity.ok(
                new PaymentResponse("Payment successful", null)
        );
    }

    @GetMapping("/failure/{orderId}")
    public ResponseEntity<PaymentResponse> paymentFailure(
            @PathVariable @Min(value = 1, message = "Invalid orderId") Integer orderId) {

        paymentService.failure(orderId);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new PaymentResponse("Payment failed", null)
        );
    }
}