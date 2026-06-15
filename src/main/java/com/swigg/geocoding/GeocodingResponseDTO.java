package com.swigg.geocoding;

import java.math.BigDecimal;

public class GeocodingResponseDTO {

    private BigDecimal lat;
    private BigDecimal lng;
    private String address;
    private String message;

    public GeocodingResponseDTO(BigDecimal lat, BigDecimal lng, String address, String message) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.message = message;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
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
