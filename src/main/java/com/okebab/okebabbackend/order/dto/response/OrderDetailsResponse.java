package com.okebab.okebabbackend.order.dto.response;

import com.okebab.okebabbackend.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        String customerName,
        String customerPhone,
        LocalDateTime pickupTime,
        List<OrderItemResponse> items
) {}
