package com.swigg.food;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodResponseDTO {

    private UUID foodId;
    private String foodName;
    private String description;
    private Integer price;
    private BigDecimal rating;
    private Integer reviewCount;
    private FoodCategory category;
    private UUID restaurantId;
    private Boolean isAvailable;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
