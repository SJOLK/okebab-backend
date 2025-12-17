package com.okebab.okebabbackend.order.controller;

import com.okebab.okebabbackend.order.dto.request.OrderRequest;
import com.okebab.okebabbackend.order.dto.request.OrderStatusUpdateRequest;
import com.okebab.okebabbackend.order.dto.response.OrderDetailsResponse;
import com.okebab.okebabbackend.order.dto.response.OrderResponse;
import com.okebab.okebabbackend.order.dto.response.OrderSummaryResponse;
import com.okebab.okebabbackend.order.model.OrderStatus;
import com.okebab.okebabbackend.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // Next.js en dev
public class OrderController {

    private final OrderService orderService;

    // =====================
    // CREATE ORDER
    // =====================
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =====================
    // LIST ORDERS (MVP)
    // =====================
    @GetMapping
    public Page<OrderSummaryResponse> listOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerPhone,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,

            Pageable pageable
    ) {
        return orderService.listOrders(status, q, customerName, customerPhone, from, to, minTotal, maxTotal, pageable);
    }

    @GetMapping("/{id}")
    public OrderDetailsResponse getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }



    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @Valid
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest body
    ) {
        OrderResponse updated = orderService.updateStatus(id, body.status());
        return ResponseEntity.ok(updated);
    }
}
