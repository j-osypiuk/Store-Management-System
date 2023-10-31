package com.example.shopapp.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByProductsProductId(Long id);
    List<Order> findAllByOrderDateBetween(LocalDateTime fromDate, LocalDateTime toDate);
    List<Order> findAllByUserUserId(Long id);
    List<Order> findAllByIsCompleted(boolean isCompleted);
}
