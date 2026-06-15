package com.swigg.rider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RiderUpdateRequestDTO {

    private UUID riderId;
    private String name;
    private String address;
    private LocalDateTime dob;
    private com.swigg.customer.Gender gender;
    private BigDecimal lat;
    private BigDecimal lng;
    private String vehicleNumber;
    private String dlNumber;

    public RiderUpdateRequestDTO() {}

    public UUID getRiderId() {
        return riderId;
    }

    public void setRiderId(UUID riderId) {
        this.riderId = riderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getDob() {
        return dob;
    }

    public void setDob(LocalDateTime dob) {
        this.dob = dob;
    }

    public com.swigg.customer.Gender getGender() {
        return gender;
    }

    public void setGender(com.swigg.customer.Gender gender) {
        this.gender = gender;
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

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getDlNumber() {
        return dlNumber;
    }

    public void setDlNumber(String dlNumber) {
        this.dlNumber = dlNumber;
    }
}
