package com.swigg.food;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodUpdateRequestDTO {

    @Size(min = 3, max = 100, message = "Food name must be between 3 and 100 characters")
    private String foodName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Min(value = 1, message = "Price must be greater than 0")
    private Integer price;

    private FoodCategory category;

    private Boolean isAvailable;
}
