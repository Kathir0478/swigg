package com.swigg.cart;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoveFoodFromCartRequestDTO {

    @NotNull(message = "Food ID is required")
    private UUID foodId;

    @NotNull(message = "Count is required")
    @Min(value = 1, message = "Count must be at least 1")
    private Integer count;
}
