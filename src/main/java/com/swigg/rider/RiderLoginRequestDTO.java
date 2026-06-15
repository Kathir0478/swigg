package com.swigg.rider;

public class RiderLoginRequestDTO {
    private String username;
    private String password;

    public RiderLoginRequestDTO() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
