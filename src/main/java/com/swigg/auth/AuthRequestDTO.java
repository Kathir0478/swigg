package com.swigg.auth;

import com.swigg.user.Role;

import java.util.UUID;

public class AuthRequestDTO {
    private UUID userId;
    private Role role;

    public AuthRequestDTO(){}

    public AuthRequestDTO(UUID userId, Role role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
