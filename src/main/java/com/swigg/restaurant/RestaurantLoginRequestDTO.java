package com.swigg.restaurant;

public class RestaurantLoginRequestDTO {

    private String username;
    private String password;

    public RestaurantLoginRequestDTO() {}

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
