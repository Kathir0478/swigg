package com.swigg.customer;

public class CustomerRegisterVerifyRequestDTO {
    private String otpCode;

    public CustomerRegisterVerifyRequestDTO() {}

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
