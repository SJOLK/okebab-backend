package com.okebab.okebabbackend.order.repository;

import com.okebab.okebabbackend.order.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
        @EntityGraph(attributePaths = "items")
        Optional<Order> findWithItemsById(Long id);
}
