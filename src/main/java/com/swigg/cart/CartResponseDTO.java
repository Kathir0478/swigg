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
public class CartResponseDTO {

    private UUID cartId;
    private UUID customerId;
    private UUID restaurantId;
    private List<UUID> foodIds;
    private BigDecimal totalPrice;
    private CartStatus status;
    private Boolean isActive;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
