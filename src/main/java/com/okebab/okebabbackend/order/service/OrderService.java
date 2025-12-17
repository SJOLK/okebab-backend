package com.okebab.okebabbackend.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okebab.okebabbackend.order.dto.request.OrderItemRequest;
import com.okebab.okebabbackend.order.dto.request.OrderRequest;
import com.okebab.okebabbackend.order.dto.response.*;
import com.okebab.okebabbackend.order.model.Order;
import com.okebab.okebabbackend.order.model.OrderItem;
import com.okebab.okebabbackend.order.model.OrderStatus;
import com.okebab.okebabbackend.order.model.ProductType;
import com.okebab.okebabbackend.order.repository.OrderRepository;
import com.okebab.okebabbackend.order.spec.OrderSpecifications;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

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

    // =====================
    // CREATE ORDER
    // =====================
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La commande doit contenir au moins un article.");
        }

        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setPickupTime(request.getPickupTime());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem();

            item.setProductIdFront(itemReq.getProductIdFront());
            item.setProductName(itemReq.getProductName());
            item.setQuantity(itemReq.getQuantity());
            item.setDescription(itemReq.getDescription());

            ProductType type = mapProductType(itemReq.getProductType());
            item.setProductType(type);

            BigDecimal basePrice = itemReq.getBasePrice() != null
                    ? itemReq.getBasePrice()
                    : BigDecimal.ZERO;

            item.setBasePrice(basePrice);

            BigDecimal lineTotal = itemReq.getTotalPrice();
            if (lineTotal == null) {
                lineTotal = basePrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            }
            item.setTotalPrice(lineTotal);

            if (itemReq.getSelections() != null) {
                try {
                    item.setSelectionsJson(
                            objectMapper.writeValueAsString(itemReq.getSelections())
                    );
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Erreur lors de la sérialisation des sélections", e);
                }
            }

            order.addItem(item);
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        return new OrderResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getTotalAmount(),
                saved.getCreatedAt()
        );
    }



    public Page<OrderListItemResponse> listOrders(
            OrderStatus status,
            String q,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        Specification<Order> spec = Specification
                .where(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.search(q))
                .and(OrderSpecifications.createdAfter(dateFrom))
                .and(OrderSpecifications.createdBefore(dateTo))
                .and(OrderSpecifications.createdBetween(dateFrom, dateTo));

        return orderRepository.findAll(spec, pageable)
                .map(o -> new OrderListItemResponse(
                        o.getId(),
                        o.getStatus(),
                        o.getTotalAmount(),
                        o.getCreatedAt(),
                        o.getCustomerName(),
                        o.getCustomerPhone(),
                        o.getPickupTime(),
                        o.getItems() != null ? o.getItems().size() : 0
                ));
    }

    // =====================
    // PRODUCT TYPE MAPPING
    // =====================
    private ProductType mapProductType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("productType est requis");
        }

        return switch (type.toLowerCase()) {
            case "tacos","taco" -> ProductType.TACOS;
            case "panini", "paninis" -> ProductType.PANINI;
            case "bowl", "bowls" -> ProductType.BOWL;
            case "assiette", "assiettes" -> ProductType.ASSIETTE;
            case "burger", "burgers" -> ProductType.BURGER;
            case "texmex", "tex mex", "tex max", "tex-max", "tex-mex" -> ProductType.TEXMEX;
            case "sandwich", "sandwiches", "sandwichs"  -> ProductType.SANDWICH;
            case "boisson", "boissons" -> ProductType.BOISSON;
            case "dessert", "desserts" -> ProductType.DESSERT;
            case "accompagnement", "accompagnements" -> ProductType.ACCOMPAGNEMENT;
            case "menu_enfant", "menu-enfant", "menu enfant" -> ProductType.MENU_ENFANT;
            case "salade", "salades" -> ProductType.SALADE;
            default -> throw new IllegalArgumentException("Type de produit inconnu : " + type);
        };
    }



    public OrderDetailsResponse getOrderById(Long id) {
        Order o = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));

        var items = o.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(),
                        i.getProductIdFront(),
                        i.getProductType(),
                        i.getProductName(),
                        i.getBasePrice(),
                        i.getTotalPrice(),
                        i.getQuantity(),
                        i.getSelectionsJson(),
                        i.getDescription()
                ))
                .toList();

        return new OrderDetailsResponse(
                o.getId(),
                o.getStatus(),
                o.getTotalAmount(),
                o.getCreatedAt(),
                o.getCustomerName(),
                o.getCustomerPhone(),
                o.getPickupTime(),
                items
        );
    }

    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Commande introuvable"));

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        return new OrderResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getTotalAmount(),
                saved.getCreatedAt()
        );
    }

    public Page<OrderSummaryResponse> listOrders(
            OrderStatus status,
            String q,
            String customerName,
            String customerPhone,
            LocalDateTime from,
            LocalDateTime to,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            Pageable pageable
    ) {
        Specification<Order> spec = Specification
                .where(OrderSpecifications.hasStatus(status))
                .and(OrderSpecifications.search(q))
                .and(OrderSpecifications.customerNameLike(customerName))
                .and(OrderSpecifications.customerPhoneLike(customerPhone))
                .and(OrderSpecifications.createdBetween(from, to))
                .and(OrderSpecifications.totalBetween(minTotal, maxTotal));

        return orderRepository.findAll(spec, pageable)
                .map(o -> new OrderSummaryResponse(
                        o.getId(),
                        o.getStatus(),
                        o.getTotalAmount(),
                        o.getCreatedAt(),
                        o.getPickupTime(),
                        o.getCustomerName(),
                        o.getCustomerPhone(),
                        o.getItems() == null ? 0 : o.getItems().size()
                ));
    }

}
