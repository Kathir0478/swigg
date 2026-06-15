package com.swigg.rider;

import com.swigg.customer.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderResponseDTO {

    private UUID riderId;
    private UUID userId;
    private String name;
    private String address;
    private LocalDateTime dob;
    private Gender gender;
    private BigDecimal lat;
    private BigDecimal lng;
    private String vehicleNumber;
    private String dlNumber;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
