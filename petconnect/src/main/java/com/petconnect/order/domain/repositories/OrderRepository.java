package com.petconnect.order.domain.repositories;

import com.petconnect.order.domain.Order;
import com.petconnect.order.domain.OrderStatus;
import java.util.List;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    List<Order> findBySellerId(UUID sellerId);
    List<Order> findByBuyerId(UUID buyerId);
    Order findById(UUID id);
    boolean existsById(UUID id);
}