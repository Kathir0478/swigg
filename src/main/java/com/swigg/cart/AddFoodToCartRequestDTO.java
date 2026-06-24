package com.swigg.cart;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFoodToCartRequestDTO {

    @NotNull(message = "Food ID is required")
    private UUID foodId;

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be at least 1")
    private Integer price;
}
