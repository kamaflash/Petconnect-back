package com.petconnect.order.domain.repositories;

import com.petconnect.order.domain.OrderItem;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository {
    OrderItem save(OrderItem item);
    List<OrderItem> findByOrderId(UUID orderId);
    List<OrderItem> findByOrderIdAndProductId(UUID orderId, UUID productId);
}