package com.okebab.okebabbackend.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper; // fourni par Spring Boot

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

            // mapping string -> enum
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

            // sérialisation du JSON des sélections
            if (itemReq.getSelections() != null) {
                try {
                    String json = objectMapper.writeValueAsString(itemReq.getSelections());
                    item.setSelectionsJson(json);
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

    private ProductType mapProductType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("productType est requis");
        }

        return switch (type.toLowerCase()) {
            case "tacos" -> ProductType.TACOS;
            case "panini", "paninis" -> ProductType.PANINI;
            case "bowl", "bowls" -> ProductType.BOWL;
            case "assiette", "assiettes" -> ProductType.ASSIETTE;
            case "burger", "burgers" -> ProductType.BURGER;
            case "texmex", "tex-max", "tex-mex" -> ProductType.TEXMEX;
            case "sandwich", "sandwiches" -> ProductType.SANDWICH;
            case "boisson", "boissons" -> ProductType.BOISSON;
            case "dessert", "desserts" -> ProductType.DESSERT;
            case "accompagnement", "accompagnements" -> ProductType.ACCOMPAGNEMENT;
            case "menu_enfant", "menu-enfant", "menu enfant" -> ProductType.MENU_ENFANT;
            case "salade", "salades" -> ProductType.SALADE;
            default -> throw new IllegalArgumentException("Type de produit inconnu : " + type);
        };
    }
}
