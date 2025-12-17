package com.okebab.okebabbackend.order.spec;

import com.okebab.okebabbackend.order.model.Order;
import com.okebab.okebabbackend.order.model.OrderItem;
import com.okebab.okebabbackend.order.model.OrderStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class OrderSpecifications {

    private OrderSpecifications() {}

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> totalBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("totalAmount"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("totalAmount"), min);
            return cb.lessThanOrEqualTo(root.get("totalAmount"), max);
        };
    }

    public static Specification<Order> customerNameLike(String customerName) {
        return (root, query, cb) -> {
            if (customerName == null || customerName.isBlank()) return null;
            return cb.like(cb.lower(root.get("customerName")), "%" + customerName.toLowerCase().trim() + "%");
        };
    }

    public static Specification<Order> customerPhoneLike(String customerPhone) {
        return (root, query, cb) -> {
            if (customerPhone == null || customerPhone.isBlank()) return null;
            return cb.like(cb.lower(root.get("customerPhone")), "%" + customerPhone.toLowerCase().trim() + "%");
        };
    }

    /**
     * Recherche "q" sur:
     * - customerName, customerPhone
     * - productName (OrderItem)
     * - id si q est numérique
     */
    public static Specification<Order> search(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return null;

            String like = "%" + q.toLowerCase().trim() + "%";

            // join items uniquement pour la recherche produit
            Join<Order, OrderItem> items = root.join("items", JoinType.LEFT);

            // IMPORTANT pour éviter les doublons quand on join
            query.distinct(true);

            var p1 = cb.like(cb.lower(root.get("customerName")), like);
            var p2 = cb.like(cb.lower(root.get("customerPhone")), like);
            var p3 = cb.like(cb.lower(items.get("productName")), like);

            // si q est un nombre -> match id
            try {
                Long id = Long.parseLong(q.trim());
                var p4 = cb.equal(root.get("id"), id);
                return cb.or(p1, p2, p3, p4);
            } catch (NumberFormatException ignored) {
                return cb.or(p1, p2, p3);
            }
        };
    }

    public static Specification<Order> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Order> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }


    public static Specification<Order> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<Order> matchesQuery(String q) {
        return (root, query, cb) -> {
            String like = "%" + q.toLowerCase() + "%";

            // Pour permettre la recherche sur les items (join) sans doublons
            query.distinct(true);

            // join vers items (si ton mapping s'appelle bien "items")
            Join<Order, OrderItem> itemsJoin = root.join("items", JoinType.LEFT);

            // match sur customerName / customerPhone / productName
            return cb.or(
                    cb.like(cb.lower(root.get("customerName")), like),
                    cb.like(cb.lower(root.get("customerPhone")), like),
                    cb.like(cb.lower(itemsJoin.get("productName")), like)
            );
        };
    }


}
