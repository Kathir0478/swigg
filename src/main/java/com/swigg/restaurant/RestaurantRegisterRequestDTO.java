package com.swigg.restaurant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RestaurantRegisterRequestDTO {

    private String description;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime opentime;
    private LocalDateTime closetime;
    private String imageurl;

    public RestaurantRegisterRequestDTO() {}

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getOpentime() {
        return opentime;
    }

    public void setOpentime(LocalDateTime opentime) {
        this.opentime = opentime;
    }

    public LocalDateTime getClosetime() {
        return closetime;
    }

    public void setClosetime(LocalDateTime closetime) {
        this.closetime = closetime;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
