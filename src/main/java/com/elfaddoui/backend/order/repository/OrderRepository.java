package com.elfaddoui.backend.order.repository;

import com.elfaddoui.backend.order.entity.Order;
import com.elfaddoui.backend.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserEmail(Long id, String email);
    Optional<Order> findByReferenceIgnoreCaseAndUserEmail(String reference, String email);
    @EntityGraph(attributePaths = {"items", "items.product"})
    Optional<Order> findWithItemsByReferenceIgnoreCaseAndUserEmail(String reference, String email);
    Optional<Order> findTopByUserEmailOrderByCreatedAtDesc(String email);
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByStatus(OrderStatus status);
    long countByCreatedAtAfter(Instant createdAt);
}
