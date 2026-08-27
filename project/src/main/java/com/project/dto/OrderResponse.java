package com.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private List<OrderItemResponse> items;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
}
