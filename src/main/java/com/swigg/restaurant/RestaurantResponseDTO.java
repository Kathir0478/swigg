package com.swigg.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponseDTO {

    private UUID restaurantId;
    private UUID userId;
    private String name;
    private String description;
    private BigDecimal lat;
    private BigDecimal lng;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String imageUrl;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
