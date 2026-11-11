package com.petconnect.order.application.dto;

import com.petconnect.order.domain.OrderStatus;

public record UpdateOrderStatusRequest(OrderStatus status) {}