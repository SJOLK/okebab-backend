package com.okebab.okebabbackend.order;

public enum OrderStatus {
    PENDING,      // commande reçue, en attente de préparation
    IN_PREPARATION,
    READY,
    COMPLETED,
    CANCELLED
}
