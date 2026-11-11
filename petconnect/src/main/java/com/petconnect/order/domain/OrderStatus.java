package com.petconnect.order.domain;

public enum OrderStatus {
    PENDING,    // Pendiente de pago
    PAID,       // Pagado
    PREPARING,  // Preparando pedido
    SHIPPED,    // Enviado
    DELIVERED,  // Entregado
    CANCELLED   // Cancelado
}