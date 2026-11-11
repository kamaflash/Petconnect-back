package com.petconnect.order.infrastructure.persistence;

import com.petconnect.order.domain.OrderItem;
import com.petconnect.order.domain.repositories.OrderItemRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaOrderItemRepositoryAdapter implements OrderItemRepository {

    private final SpringDataOrderItemRepository repository;

    public JpaOrderItemRepositoryAdapter(SpringDataOrderItemRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public OrderItem save(OrderItem item) {
        return repository.save(item);
    }

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId);
    }

    @Override
    public List<OrderItem> findByOrderIdAndProductId(UUID orderId, UUID productId) {
        return repository.findByOrderId(orderId).stream()
                .filter(item -> productId.equals(item.getProductId()))
                .toList();
    }
}