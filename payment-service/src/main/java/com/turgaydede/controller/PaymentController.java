package com.turgaydede.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping
    public ResponseEntity<String> savePayment() {
        if (Math.random() > 0.5) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Mock 500 error");
        }
        return ResponseEntity.ok("Payment success");
    }
}
