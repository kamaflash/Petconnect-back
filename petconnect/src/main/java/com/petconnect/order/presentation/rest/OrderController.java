package com.petconnect.order.presentation.rest;

import com.petconnect.order.application.dto.CreateOrderRequest;
import com.petconnect.order.application.dto.OrderResponse;
import com.petconnect.order.application.dto.UpdateOrderStatusRequest;
import com.petconnect.order.application.usecases.CreateOrderUseCase;
import com.petconnect.order.application.usecases.GetOrderDetailUseCase;
import com.petconnect.order.application.usecases.GetOrdersBySellerUseCase;
import com.petconnect.order.application.usecases.UpdateOrderStatusUseCase;
import com.petconnect.shared.infrastructure.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order-management")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrdersBySellerUseCase getOrdersBySellerUseCase;
    private final GetOrderDetailUseCase getOrderDetailUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
            GetOrdersBySellerUseCase getOrdersBySellerUseCase,
            GetOrderDetailUseCase getOrderDetailUseCase,
            UpdateOrderStatusUseCase updateOrderStatusUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrdersBySellerUseCase = getOrdersBySellerUseCase;
        this.getOrderDetailUseCase = getOrderDetailUseCase;
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
    }

    /** Crear un nuevo pedido (cliente compra a un vendedor) */
    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateOrderRequest request) {
        var response = createOrderUseCase.execute(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Listar todos los pedidos de mi tienda (solo vendedor) */
    @GetMapping("/orders/seller")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OrderResponse> orders = getOrdersBySellerUseCase.execute(userDetails.getUserId());
        return ResponseEntity.ok(orders);
    }

    /** Obtener detalle de un pedido (solo vendedor dueño) */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID orderId) {
        OrderResponse order = getOrderDetailUseCase.execute(userDetails.getUserId(), orderId);
        return ResponseEntity.ok(order);
    }

    /** Actualizar el estado de un pedido (solo vendedor dueño) */
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse order = updateOrderStatusUseCase.execute(
                userDetails.getUserId(), orderId, request.status());
        return ResponseEntity.ok(order);
    }
}