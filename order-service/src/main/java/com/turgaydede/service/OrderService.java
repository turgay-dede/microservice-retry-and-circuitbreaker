package com.turgaydede.service;

import com.turgaydede.exception.OrderNotFoundException;
import com.turgaydede.exception.OrderProcessingException;
import com.turgaydede.feign.OrderFeignClient;
import com.turgaydede.model.Order;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderFeignClient feignClient;
    private int callCount = 0;

    public OrderService(OrderFeignClient feignClient) {
        this.feignClient = feignClient;
    }
    @Retry(name = "orderServiceRetry")
    @CircuitBreaker(name = "orderServiceCircuitBreaker", fallbackMethod = "getOrdersFallback")
    public List<Order> processOrder() {
        logRequest();
        String paymentResponse = feignClient.savePayment().getBody();
        System.out.println("Ödeme servisi yanıtı: " + paymentResponse);
        return mockOrders();
    }

    @RateLimiter(name = "orderServiceRateLimiter", fallbackMethod = "getOrderByIdFallback")
    public Order getOrderById(String orderId) {
        return mockOrders().stream()
                .filter(order -> orderId.equals(order.getOrderId()))
                .findFirst()
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    public Order getOrderByIdFallback(String orderId, RequestNotPermitted ex) {
        System.out.println("Rate limiter triggered for getOrderById - orderId: "+ orderId + ", reason: " + ex.getMessage());
        return fallbackOrder(orderId);
    }

    private Order fallbackOrder(String orderId) {
        return Order.builder()
                .orderId(orderId)
                .customerName("fallback-customer")
                .product("N/A")
                .quantity(0)
                .status("FALLBACK")
                .build();
    }



    public List<Order> getOrdersFallback(Throwable t) {
        System.err.println("Fallback devreye girdi: " + t.getMessage());
        throw new OrderProcessingException("Payment servisinden yanıt alınamadı. Sipariş işlenemedi.", t);
    }


    private void logRequest() {
        callCount++;
        System.out.println("processOrder çağrısı #" + callCount);
    }

    private List<Order> mockOrders() {
        return List.of(
                new Order("ORD-1001", "Ahmet Yılmaz", "Laptop", 1, "PROCESSING"),
                new Order("ORD-1002", "Ayşe Demir", "Mouse", 2, "SHIPPED")
        );
    }
}
