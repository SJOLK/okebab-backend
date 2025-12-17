package com.okebab.okebabbackend.order.model;

public enum OrderStatus {
    PENDING,      // commande reçue, en attente de préparation
    IN_PREPARATION,
    READY,
    COMPLETED,
    CANCELLED
}
