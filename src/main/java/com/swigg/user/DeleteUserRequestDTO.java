package com.swigg.user;

public class DeleteUserRequestDTO {

    private String otpCode;

    public DeleteUserRequestDTO() {}

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}
