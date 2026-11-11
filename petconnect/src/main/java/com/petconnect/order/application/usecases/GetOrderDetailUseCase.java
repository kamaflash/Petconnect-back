package com.petconnect.order.application.usecases;

import com.petconnect.order.application.dto.OrderItemResponse;
import com.petconnect.order.application.dto.OrderResponse;
import com.petconnect.order.domain.Order;
import com.petconnect.order.domain.OrderItem;
import com.petconnect.order.domain.repositories.OrderItemRepository;
import com.petconnect.order.domain.repositories.OrderRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetOrderDetailUseCase {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserProfileRepository userProfileRepository;

    public GetOrderDetailUseCase(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserProfileRepository userProfileRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional(readOnly = true)
    public OrderResponse execute(UUID sellerId, UUID orderId) {
        UserProfile seller = userProfileRepository.findByAuthUserId(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        if (!"seller".equalsIgnoreCase(seller.getProfileType())) {
            throw new RuntimeException("This feature is only available to sellers");
        }

        Order order = orderRepository.findById(orderId);
        if (!order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("You are not the owner of this order");
        }

        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponses = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getProductImageUrl(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getSellerId(),
                order.getBuyerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getPaymentMethod(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}