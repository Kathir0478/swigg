package com.swigg.order;

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
public class OrderResponseDTO {

    private UUID orderId;
    private UUID customerId;
    private UUID riderId;
    private UUID restaurantId;
    private UUID cartId;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;
    private String deliveryAddress;
    private OrderStatus status;
    private String customerInstructions;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
