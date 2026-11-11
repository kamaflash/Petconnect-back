package com.petconnect.order.application.usecases;

import com.petconnect.order.application.dto.OrderItemResponse;
import com.petconnect.order.application.dto.OrderResponse;
import com.petconnect.order.domain.Order;
import com.petconnect.order.domain.OrderItem;
import com.petconnect.order.domain.OrderStatus;
import com.petconnect.order.domain.repositories.OrderItemRepository;
import com.petconnect.order.domain.repositories.OrderRepository;
import com.petconnect.shared.infrastructure.websocket.NotificationService;
import com.petconnect.social.domain.Notification;
import com.petconnect.social.infrastructure.persistence.SpringDataNotificationRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UpdateOrderStatusUseCase {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserProfileRepository userProfileRepository;
    private final SpringDataNotificationRepository notificationRepository;
    private final ObjectProvider<NotificationService> notificationServiceProvider;

    public UpdateOrderStatusUseCase(OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserProfileRepository userProfileRepository,
            SpringDataNotificationRepository notificationRepository,
            ObjectProvider<NotificationService> notificationServiceProvider) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userProfileRepository = userProfileRepository;
        this.notificationRepository = notificationRepository;
        this.notificationServiceProvider = notificationServiceProvider;
    }

    @Transactional
    public OrderResponse execute(UUID sellerId, UUID orderId, OrderStatus newStatus) {
        // Validar que el vendedor exista y sea tipo seller
        UserProfile seller = userProfileRepository.findByAuthUserId(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        if (!"seller".equalsIgnoreCase(seller.getProfileType())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "This feature is only available to sellers");
        }

        Order order = orderRepository.findById(orderId);
        if (!order.getSellerId().equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not the owner of this order");
        }

        order.updateStatus(newStatus);
        Order saved = orderRepository.save(order);

        // Notificar al comprador sobre el cambio de estado
        notifyBuyer(saved.getId(), saved.getBuyerId(), newStatus);

        return toResponse(saved);
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

    private void notifyBuyer(UUID orderId, UUID buyerId, OrderStatus newStatus) {
        Notification notification = new Notification(buyerId, "ORDER_STATUS_CHANGED",
                "Tu pedido #" + orderId + " ha cambiado a: " + newStatus, null);
        notification.setReference(orderId, "ORDER");
        notificationRepository.save(notification);

        NotificationService notificationService = notificationServiceProvider.getIfAvailable();
        if (notificationService != null) {
            try {
                notificationService.sendToUser(buyerId, "ORDER_STATUS_CHANGED", notification);
            } catch (RuntimeException e) {
                // No bloquear si falla WebSocket
            }
        }
    }
}