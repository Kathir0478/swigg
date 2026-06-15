package com.swigg.user;

public class LoginVerifyRequestDTO {

    private String phoneNumber;
    private String otpCode;

    public LoginVerifyRequestDTO() {}

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
