package com.okebab.okebabbackend.order.dto.response;

import com.okebab.okebabbackend.order.model.ProductType;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productIdFront,
        ProductType productType,
        String productName,
        BigDecimal basePrice,
        BigDecimal totalPrice,
        int quantity,
        String selectionsJson,
        String description
) {}
