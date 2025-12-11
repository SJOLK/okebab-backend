package com.okebab.okebabbackend.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderItemRequest {

    private Long productIdFront;       // id côté front (ex: 1 pour Tacos M)
    private String productType;        // "tacos", "panini", "bowls", ...

    private String productName;
    private BigDecimal basePrice;      // prix unitaire de base
    private BigDecimal totalPrice;     // prix total de la LIGNE (suppléments * quantité)
    private int quantity;

    private String description;        // texte lisible (comme dans ton panier)

    // structure des choix: meats, sauces, supplements, ...
    private Map<String, Object> selections;
}
