package com.swigg.order;

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
public class RiderNotificationResponseDTO {

    private UUID notificationId;
    private UUID riderId;
    private UUID cartId;
    private UUID customerId;
    private UUID restaurantId;
    private RiderNotification.NotificationStatus status;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
