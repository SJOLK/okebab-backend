package com.okebab.okebabbackend.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // on pourra ajouter des méthodes de recherche plus tard (par statut, date, etc.)
}
