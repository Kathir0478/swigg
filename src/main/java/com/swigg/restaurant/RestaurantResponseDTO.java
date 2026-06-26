package com.swigg.restaurant;

import lombok.*;

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
    private LocalTime openTime;
    private LocalTime closeTime;
    private String imageUrl;
    private String address;
    private BigDecimal rating;
    private Integer reviewCount;
    private BigDecimal lat;
    private BigDecimal lng;
    private Boolean isActive;
    private Boolean isVerified;
    private BigDecimal distance; // Distance from customer in km
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
