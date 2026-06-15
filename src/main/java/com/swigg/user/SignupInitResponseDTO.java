package com.swigg.user;

public class SignupInitResponseDTO {

    private String username;
    private String message;
    private String maskedPhoneNumber;

    public SignupInitResponseDTO() {}

    public SignupInitResponseDTO(String username, String message, String maskedPhoneNumber) {
        this.username = username;
        this.message = message;
        this.maskedPhoneNumber = maskedPhoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMaskedPhoneNumber() {
        return maskedPhoneNumber;
    }

    public void setMaskedPhoneNumber(String maskedPhoneNumber) {
        this.maskedPhoneNumber = maskedPhoneNumber;
    }
}
