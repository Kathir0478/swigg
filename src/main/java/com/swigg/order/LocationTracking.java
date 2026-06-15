package com.swigg.order;

import com.swigg.order.Order;
import com.swigg.rider.Rider;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "location_tracking", indexes = {
        @Index(name = "idx_location_tracking_orderid", columnList = "orderid"),
        @Index(name = "idx_location_tracking_riderid", columnList = "riderid"),
        @Index(name = "idx_location_tracking_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID trackingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderid", nullable = false)
    private Order order;

    @Column(name = "orderid", insertable = false, updatable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "riderid", nullable = false)
    private Rider rider;

    @Column(name = "riderid", insertable = false, updatable = false)
    private UUID riderId;

    @NotNull(message = "Latitude is required for location tracking")
    @DecimalMin(value = "-90.000000", message = "Latitude out of bounds")
    @DecimalMax(value = "90.000000", message = "Latitude out of bounds")
    @Column(name = "lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal lat;

    @NotNull(message = "Longitude is required for location tracking")
    @DecimalMin(value = "-180.000000", message = "Longitude out of bounds")
    @DecimalMax(value = "180.000000", message = "Longitude out of bounds")
    @Column(name = "lng", nullable = false, precision = 11, scale = 6)
    private BigDecimal lng;

    @Column(name = "accuracy")
    private BigDecimal accuracy;

    @Column(name = "speed")
    private BigDecimal speed;

    @Column(name = "heading")
    private BigDecimal heading;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
