package com.swigg.order.messaging;

import com.swigg.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusMessage {

    private UUID orderId;
    private UUID customerId;
    private UUID riderId;
    private UUID restaurantId;
    private OrderStatus status;
    private LocalDateTime timestamp;
}
