package com.petconnect.order.infrastructure.persistence;

import com.petconnect.order.domain.Order;
import com.petconnect.order.domain.OrderStatus;
import com.petconnect.order.domain.repositories.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataOrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findBySellerId(UUID sellerId);

    List<Order> findByBuyerId(UUID buyerId);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByStatus(OrderStatus status);

    Optional<Order> findByIdAndSellerId(UUID id, UUID sellerId);
}