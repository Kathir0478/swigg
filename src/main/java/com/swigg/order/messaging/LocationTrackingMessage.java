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
public class LocationTrackingMessage {

    private UUID trackingId;
    private UUID orderId;
    private UUID riderId;
    private UUID customerId;
    private BigDecimal lat;
    private BigDecimal lng;
    private BigDecimal accuracy;
    private BigDecimal speed;
    private BigDecimal heading;
    private LocalDateTime timestamp;
}
