package com.swigg.order.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiderNotificationMessage {

    private UUID notificationId;
    private UUID riderId;
    private UUID cartId;
    private UUID customerId;
    private UUID restaurantId;
    private BigDecimal restaurantLat;
    private BigDecimal restaurantLng;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;
    private String deliveryAddress;
    private LocalDateTime createdAt;
}
