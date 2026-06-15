package com.swigg.cart;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private UUID foodId;
    private Integer quantity;
}
