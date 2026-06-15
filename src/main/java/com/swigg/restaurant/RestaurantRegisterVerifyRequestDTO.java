package com.swigg.restaurant;

public class RestaurantRegisterVerifyRequestDTO {

    private String phoneNumber;
    private String otpCode;

    public RestaurantRegisterVerifyRequestDTO() {}

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
