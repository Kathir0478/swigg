package com.swigg.rider;

public class RiderRegisterVerifyRequestDTO {
    private String otpCode;

    public RiderRegisterVerifyRequestDTO() {}

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
