package com.swigg.auth;

public class LoginVerifyRequestDTO {

    private String username;
    private String otpCode;

    public LoginVerifyRequestDTO() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
