package com.swigg.restaurant;

public class RestaurantDeleteVerifyDTO {
    private String otpCode;

    public RestaurantDeleteVerifyDTO(){}

    public RestaurantDeleteVerifyDTO(String otpCode) {
        this.otpCode = otpCode;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
