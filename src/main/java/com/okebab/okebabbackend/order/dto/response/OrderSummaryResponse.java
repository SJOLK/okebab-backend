package com.okebab.okebabbackend.order.dto.response;

import com.okebab.okebabbackend.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime pickupTime,
        String customerName,
        String customerPhone,
        int itemsCount
) {}
