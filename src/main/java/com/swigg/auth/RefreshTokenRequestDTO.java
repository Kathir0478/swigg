package com.swigg.auth;

public class RefreshTokenRequestDTO {

    private String refreshToken;

    public RefreshTokenRequestDTO() {}

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
