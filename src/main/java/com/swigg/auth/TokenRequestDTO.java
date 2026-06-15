package com.swigg.auth;

public class TokenRequestDTO {

    private String username;
    private String password;

    public TokenRequestDTO() {}

    public TokenRequestDTO(String password, String username) {
        this.password = password;
        this.username = username;
    }

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
