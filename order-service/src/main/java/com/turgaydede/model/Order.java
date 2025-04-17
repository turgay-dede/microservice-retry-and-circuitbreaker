package com.turgaydede.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    private String orderId;
    private String customerName;
    private String product;
    private int quantity;
    private String status;
}
