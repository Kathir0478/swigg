package com.swigg.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private boolean success;
    private String error;

    public ErrorResponse(String error) {
        this.success = false;
        this.error = error;
    }
}
