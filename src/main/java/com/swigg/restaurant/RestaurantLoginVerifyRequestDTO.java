package com.swigg.restaurant;

public class RestaurantLoginVerifyRequestDTO {

    private String phoneNumber;
    private String otpCode;

    public RestaurantLoginVerifyRequestDTO() {}

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
