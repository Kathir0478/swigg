package com.swigg.order;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class OrderRequestDTO {

    @NotNull(message = "Cart ID is required")
    private UUID cartId;

    @NotNull(message = "Delivery latitude is required")
    @DecimalMin(value = "-90.000000", message = "Latitude out of bounds")
    @DecimalMax(value = "90.000000", message = "Latitude out of bounds")
    private BigDecimal deliveryLat;

    @NotNull(message = "Delivery longitude is required")
    @DecimalMin(value = "-180.000000", message = "Longitude out of bounds")
    @DecimalMax(value = "180.000000", message = "Longitude out of bounds")
    private BigDecimal deliveryLng;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String customerInstructions;
}
