package com.swigg.rider;

public class RiderLoginVerifyRequestDTO {
    private String phoneNumber;
    private String otpCode;

    public RiderLoginVerifyRequestDTO() {}

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
