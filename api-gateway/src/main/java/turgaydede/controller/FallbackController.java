package turgaydede.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/orders")
    public ResponseEntity<String> ordersFallback() {
        return ResponseEntity.ok("Order Service şu anda kullanılamıyor.");
    }

    @GetMapping("/payments")
    public ResponseEntity<String> paymentsFallback() {
        return ResponseEntity.ok("Payment Service şu anda kullanılamıyor.");
    }
}
