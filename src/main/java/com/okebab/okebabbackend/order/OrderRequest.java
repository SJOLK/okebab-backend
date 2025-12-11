package com.okebab.okebabbackend.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderRequest {

    private String customerName;
    private String customerPhone;

    // Heure de retrait souhaitée (optionnelle)
    private LocalDateTime pickupTime;

    // total calculé côté front (optionnel, pour comparaison)
    private BigDecimal cartTotal;

    private List<OrderItemRequest> items;
}
