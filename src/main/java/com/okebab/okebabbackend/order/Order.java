package com.okebab.okebabbackend.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // "order" est un mot réservé SQL, donc "orders"
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "items")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quand la commande a été créée
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Montant total payé / à payer
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Statut de la commande
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // Infos client (simples pour l’instant)
    private String customerName;

    private String customerPhone;

    // Heure souhaitée de retrait (optionnelle)
    private LocalDateTime pickupTime;

    // Lignes de commande
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    // Petit helper pratique
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}
