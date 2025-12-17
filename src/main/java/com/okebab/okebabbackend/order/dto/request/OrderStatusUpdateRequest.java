package com.okebab.okebabbackend.order.dto.request;

import com.okebab.okebabbackend.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull OrderStatus status
) {}
