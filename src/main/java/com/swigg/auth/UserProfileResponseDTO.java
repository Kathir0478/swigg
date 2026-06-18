package com.swigg.auth;

import com.swigg.customer.Gender;
import com.swigg.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponseDTO {
    private boolean success;
    private String message;
    private UserProfileData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileData {
        private List<String> roles;
        private List<String> registeredRoles;
        private String userId;
        private String email;
        private String phone;
        
        // Rider fields
        private String riderId;
        private String riderName;
        private String riderAddress;
        private LocalDateTime riderDob;
        private Gender riderGender;
        private BigDecimal riderLat;
        private BigDecimal riderLng;
        private String riderVehicleNumber;
        private String riderDlNumber;
        private Boolean riderIsActive;
        private Boolean riderIsVerified;
        private LocalDateTime riderCreatedAt;
        private LocalDateTime riderUpdatedAt;
        
        // Restaurant fields
        private String restaurantId;
        private String restaurantName;
        private String restaurantDescription;
        private String restaurantAddress;
        private BigDecimal restaurantLat;
        private BigDecimal restaurantLng;
        private String restaurantImageUrl;
        private java.time.LocalTime restaurantOpenTime;
        private java.time.LocalTime restaurantCloseTime;
        private Boolean restaurantIsActive;
        private Boolean restaurantIsVerified;
        private LocalDateTime restaurantCreatedAt;
        private LocalDateTime restaurantUpdatedAt;
        
        // Customer fields
        private String customerId;
        private String customerAddress;
        private LocalDateTime customerDob;
        private Gender customerGender;
        private BigDecimal customerLat;
        private BigDecimal customerLng;
        private Boolean customerIsActive;
        private Boolean customerIsVerified;
        private LocalDateTime customerCreatedAt;
        private LocalDateTime customerUpdatedAt;
    }
}
