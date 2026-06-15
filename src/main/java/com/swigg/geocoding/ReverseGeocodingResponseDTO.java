package com.swigg.geocoding;

public class ReverseGeocodingResponseDTO {

    private String address;
    private String message;

    public ReverseGeocodingResponseDTO(String address, String message) {
        this.address = address;
        this.message = message;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
