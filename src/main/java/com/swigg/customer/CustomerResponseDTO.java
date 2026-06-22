package com.swigg.customer;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponseDTO {

    private UUID customerId;
    private UUID userId;
    private String name;
    private String address;
    private LocalDateTime dob;
    private Gender gender;
    private BigDecimal lat;
    private BigDecimal lng;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
