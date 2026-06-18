package com.swigg.restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swigg.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "restaurants", indexes = {
    @Index(name = "idx_restaurants_userid", columnList = "userid"),
    @Index(name = "idx_restaurants_isactive_isverified", columnList = "isactive,isverified"),
    @Index(name = "idx_restaurants_geo", columnList = "lat,lng")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "restaurantid", nullable = false, updatable = false)
    private UUID restaurantId;

    @Column(name = "userid", nullable = false, updatable = false)
    private UUID userId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userid", referencedColumnName = "userid", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_restaurants_users"))
    private User user;

    @NotBlank(message = "Restaurant name cannot be left blank")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "opentime")
    private LocalTime openTime;

    @Column(name = "closetime")
    private LocalTime closeTime;

    @Column(name = "imageurl", length = 255)
    private String imageUrl;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Builder.Default
    @DecimalMin(value = "0.0", message = "Rating cannot be lower than 0.0")
    @DecimalMax(value = "5.0", message = "Rating cannot be higher than 5.0")
    @Column(name = "rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal rating = BigDecimal.valueOf(0.0);

    @Builder.Default
    @Min(value = 0, message = "Review count cannot be negative")
    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    // --- High-Precision Spatial References ---
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