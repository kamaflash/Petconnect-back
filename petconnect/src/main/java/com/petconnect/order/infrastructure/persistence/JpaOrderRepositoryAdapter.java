package com.petconnect.order.infrastructure.persistence;

import com.petconnect.order.domain.Order;
import com.petconnect.order.domain.repositories.OrderRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaOrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    public JpaOrderRepositoryAdapter(SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Order save(Order order) {
        return repository.save(order);
    }

    @Override
    public List<Order> findBySellerId(UUID sellerId) {
        return repository.findBySellerId(sellerId);
    }

    @Override
    public List<Order> findByBuyerId(UUID buyerId) {
        return repository.findByBuyerId(buyerId);
    }

    @Override
    public Order findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}