package com.swigg.cart;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartRequestDTO {

    @NotNull(message = "Restaurant ID is required")
    private UUID restaurantId;
}
