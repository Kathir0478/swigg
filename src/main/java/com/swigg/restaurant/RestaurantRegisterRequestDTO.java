package com.swigg.restaurant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RestaurantRegisterRequestDTO {

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal lat;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal lng;

    @NotNull(message = "Opening time is required")
    private LocalDateTime opentime;

    @NotNull(message = "Closing time is required")
    private LocalDateTime closetime;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
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
