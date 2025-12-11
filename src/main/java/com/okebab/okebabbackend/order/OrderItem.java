package com.okebab.okebabbackend.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "order")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // id du produit côté front (ex: tacos M = 1)
    private Long productIdFront;

    // type du produit (TACOS, PANINI, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductType productType;

    // nom du produit au moment de la commande
    @Column(nullable = false)
    private String productName;

    // prix de base (sans suppléments)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    // prix total de CETTE LIGNE (avec suppléments * quantité)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private int quantity;

    // JSON des sélections (meats, sauces, supplements, etc.)
    @Column(columnDefinition = "TEXT")
    private String selectionsJson;

    // Description lisible (comme ce que tu affiches dans le panier)
    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
