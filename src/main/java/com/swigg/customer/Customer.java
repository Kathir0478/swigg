package com.swigg.customer;

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
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customerid", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "userid", nullable = false, updatable = false)
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userid", referencedColumnName = "userid", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_customers_users"))
    private User user;

    @NotBlank(message = "Customer name cannot be left blank")
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

    @Builder.Default
    @Column(name = "isactive", nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "isverified", nullable = false)
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "createdat", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updatedat")
    private LocalDateTime updatedAt;
}
