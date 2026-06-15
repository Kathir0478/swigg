package com.swigg.rider;

public class RiderInitResponseDTO {

    private String message;
    private String maskedPhoneNumber;

    public RiderInitResponseDTO(String message, String maskedPhoneNumber) {
        this.message = message;
        this.maskedPhoneNumber = maskedPhoneNumber;
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
