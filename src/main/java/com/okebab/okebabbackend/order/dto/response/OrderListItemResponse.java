package com.okebab.okebabbackend.order.dto.response;

import com.okebab.okebabbackend.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderListItemResponse(
        Long id,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        String customerName,
        String customerPhone,
        LocalDateTime pickupTime,
        int itemsCount
) {}
