package com.swigg.geocoding;

import java.math.BigDecimal;

public class ReverseGeocodingRequestDTO {

    private BigDecimal lat;
    private BigDecimal lng;

    public ReverseGeocodingRequestDTO() {}

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
}
