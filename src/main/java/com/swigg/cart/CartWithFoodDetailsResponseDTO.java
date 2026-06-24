package com.swigg.cart;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartWithFoodDetailsResponseDTO {

    private UUID cartId;
    private UUID customerId;
    private UUID restaurantId;
    private List<FoodItemDetail> foodItems;
    private BigDecimal totalPrice;
    private CartStatus status;
    private Boolean isActive;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodItemDetail {
        private UUID foodId;
        private String foodName;
        private String description;
        private Integer price;
        private String category;
        private Integer quantity;
    }
}
