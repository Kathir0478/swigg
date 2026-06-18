package com.swigg.rider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swigg.customer.Gender;
import com.swigg.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "riders", indexes = {
    @Index(name = "idx_riders_userid", columnList = "userid"),
    @Index(name = "idx_riders_isactive_isverified", columnList = "isactive,isverified"),
    @Index(name = "idx_riders_geo", columnList = "lat,lng")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "riderid", nullable = false, updatable = false)
    private UUID riderId;

    @Column(name = "userid", nullable = false, updatable = false)
    private UUID userId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userid", referencedColumnName = "userid", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_riders_users"))
    private User user;

    @NotBlank(message = "Rider name cannot be left blank")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name="dob",nullable = false)
    private LocalDateTime dob;

    @Enumerated(EnumType.STRING)
    @Column(name="gender",nullable = false)
    private Gender gender;

    @NotNull(message = "Latitude coordinate is required for spatial operations")
    @DecimalMin(value = "-90.000000", message = "Latitude out of bounds")
    @DecimalMax(value = "90.000000", message = "Latitude out of bounds")
    @Column(name = "lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal lat;

    @NotNull(message = "Longitude coordinate is required for spatial operations")
    @DecimalMin(value = "-180.000000", message = "Longitude out of bounds")
    @DecimalMax(value = "180.000000", message = "Longitude out of bounds")
    @Column(name = "lng", nullable = false, precision = 11, scale = 6)
    private BigDecimal lng;

    @Column(name="vehiclenumber",nullable = false)
    private String vehicleNumber;

    @Column(name = "dlnumber",nullable = false)
    private String dlNumber;

    @Builder.Default
    @Column(name = "isactive", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "isverified", nullable = false)
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "createdat", updatable = false)
    @JsonIgnore
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedat")
    @JsonIgnore
    private LocalDateTime updatedAt;
}

