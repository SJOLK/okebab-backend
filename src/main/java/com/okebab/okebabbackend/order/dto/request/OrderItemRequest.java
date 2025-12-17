package com.okebab.okebabbackend.order.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemRequest {

    // front: "id"  -> backend: productIdFront
    @JsonAlias({"id", "productIdFront"})
    @NotNull(message = "id (productIdFront) est requis")
    private Long productIdFront;

    // front: "name" -> backend: productName
    @JsonAlias({"name", "productName"})
    @NotBlank(message = "name (productName) est requis")
    private String productName;

    // front: "type" -> backend: productType (string)
    @JsonAlias({"type", "productType"})
    @NotBlank(message = "type (productType) est requis")
    private String productType;

    @NotNull(message = "basePrice est requis")
    private BigDecimal basePrice;

    // front: "totalPrice" peut être string ("14.80") -> BigDecimal OK
    @NotNull(message = "totalPrice est requis")
    private BigDecimal totalPrice;

    @Min(value = 1, message = "quantity doit être >= 1")
    private int quantity;

    private String description;

    // selections est un objet (pains, sauces, etc.) => flexible
    private Map<String, Object> selections;
}
