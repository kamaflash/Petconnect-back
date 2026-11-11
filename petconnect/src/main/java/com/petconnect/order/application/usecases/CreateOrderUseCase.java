package com.petconnect.order.application.usecases;

import com.petconnect.order.application.dto.CreateOrderRequest;
import com.petconnect.order.application.dto.OrderItemResponse;
import com.petconnect.order.application.dto.OrderResponse;
import com.petconnect.order.domain.Order;
import com.petconnect.order.domain.OrderItem;
import com.petconnect.order.domain.repositories.OrderItemRepository;
import com.petconnect.order.domain.repositories.OrderRepository;
import com.petconnect.users.domain.UserProfile;
import com.petconnect.users.domain.repositories.UserProfileRepository;
import com.petconnect.shared.infrastructure.websocket.NotificationService;
import com.petconnect.social.domain.Notification;
import com.petconnect.social.infrastructure.persistence.SpringDataNotificationRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserProfileRepository userProfileRepository;
    private final SpringDataNotificationRepository notificationRepository;
    private final ObjectProvider<NotificationService> notificationServiceProvider;

    public CreateOrderUseCase(OrderRepository orderRepository,
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
    public OrderResponse execute(UUID sellerId, CreateOrderRequest request) {
        // Validar que el vendedor exista y sea tipo seller
        UserProfile seller = userProfileRepository.findByAuthUserId(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller profile not found"));
        if (!"seller".equalsIgnoreCase(seller.getProfileType())) {
            throw new RuntimeException("This feature is only available to sellers");
        }

        // Validar que el comprador exista
        UserProfile buyer = userProfileRepository.findByAuthUserId(request.buyerId())
                .orElseThrow(() -> new RuntimeException("Buyer profile not found"));

        // Calcular total
        BigDecimal total = request.items().stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Crear orden
        Order order = new Order(sellerId, request.buyerId(), total,
                request.shippingAddress(), request.paymentMethod());
        Order savedOrder = orderRepository.save(order);

        // Crear items de la orden
        List<OrderItemResponse> itemResponses = request.items().stream()
                .map(itemReq -> {
                    OrderItem item = new OrderItem(
                            savedOrder.getId(),
                            itemReq.productId(),
                            itemReq.productName(),
                            itemReq.productImageUrl(),
                            itemReq.unitPrice(),
                            itemReq.quantity()
                    );
                    OrderItem savedItem = orderItemRepository.save(item);
                    return new OrderItemResponse(
                            savedItem.getId(),
                            savedItem.getProductId(),
                            savedItem.getProductName(),
                            savedItem.getProductImageUrl(),
                            savedItem.getUnitPrice(),
                            savedItem.getQuantity(),
                            savedItem.getSubtotal()
                    );
                })
                .toList();

        // Notificar al vendedor sobre el nuevo pedido
        notifySeller(savedOrder.getId(), sellerId, request.buyerId(), total);

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getSellerId(),
                savedOrder.getBuyerId(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount(),
                savedOrder.getShippingAddress(),
                savedOrder.getPaymentMethod(),
                itemResponses,
                savedOrder.getCreatedAt(),
                savedOrder.getUpdatedAt()
        );
    }

    private void notifySeller(UUID orderId, UUID sellerId, UUID buyerId, BigDecimal total) {
        Notification notification = new Notification(sellerId, "NEW_ORDER",
                "Nuevo pedido #" + orderId + " por " + total, buyerId);
        notification.setReference(orderId, "ORDER");
        notificationRepository.save(notification);

        NotificationService notificationService = notificationServiceProvider.getIfAvailable();
        if (notificationService != null) {
            try {
                notificationService.sendToUser(sellerId, "NEW_ORDER", notification);
            } catch (RuntimeException e) {
                // No bloquear si falla WebSocket
            }
        }
    }
}