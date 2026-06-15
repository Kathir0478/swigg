package com.swigg.order;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationTrackingRequestDTO {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Rider ID is required")
    private UUID riderId;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.000000", message = "Latitude out of bounds")
    @DecimalMax(value = "90.000000", message = "Latitude out of bounds")
    private BigDecimal lat;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.000000", message = "Longitude out of bounds")
    @DecimalMax(value = "180.000000", message = "Longitude out of bounds")
    private BigDecimal lng;

    private BigDecimal accuracy;
    private BigDecimal speed;
    private BigDecimal heading;
}
