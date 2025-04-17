package com.turgaydede.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "payment-service",url = "http://localhost:8085/api/payments")
public interface OrderFeignClient {

    @PostMapping
    ResponseEntity<String> savePayment();
}
