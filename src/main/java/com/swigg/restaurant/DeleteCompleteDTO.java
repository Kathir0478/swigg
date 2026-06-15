package com.swigg.restaurant;

import java.util.UUID;

public class DeleteCompleteDTO {

    private UUID restaurantId;
    private String totpCode;

    public DeleteCompleteDTO(){}

    public DeleteCompleteDTO(String totpCode){
        this.totpCode=totpCode;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }
}
