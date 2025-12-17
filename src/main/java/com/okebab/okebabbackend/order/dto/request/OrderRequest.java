package com.okebab.okebabbackend.order.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {

    @Valid
    @NotEmpty(message = "La commande doit contenir au moins un article.")
    private List<OrderItemRequest> items;

    @Size(max = 100, message = "Le nom client ne doit pas dépasser 100 caractères")
    private String customerName;

    @Pattern(
            regexp = "^[0-9+ ]{8,20}$",
            message = "Numéro de téléphone invalide"
    )private String customerPhone;

    // Heure de retrait souhaitée (optionnelle)
    private LocalDateTime pickupTime;


}
