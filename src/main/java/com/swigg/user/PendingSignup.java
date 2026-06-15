package com.swigg.user;

import java.time.LocalDateTime;

public class PendingSignup {

    private final String username;
    private final String phoneNumber;
    private final String password;
    private final String totpSecret;
    private final LocalDateTime createdAt;

    public PendingSignup(String username, String phoneNumber, String password, String totpSecret) {
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.totpSecret = totpSecret;
        this.createdAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getTotpSecret() {
        return totpSecret;
    }
}
