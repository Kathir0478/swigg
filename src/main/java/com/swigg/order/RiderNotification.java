package com.swigg.order;

import com.swigg.cart.Cart;
import com.swigg.customer.Customer;
import com.swigg.restaurant.Restaurant;
import com.swigg.rider.Rider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rider_notifications", indexes = {
        @Index(name = "idx_rider_notifications_riderid", columnList = "riderid"),
        @Index(name = "idx_rider_notifications_cartid", columnList = "cartid"),
        @Index(name = "idx_rider_notifications_status", columnList = "status"),
        @Index(name = "idx_rider_notifications_riderid_status", columnList = "riderid,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "riderid", nullable = false)
    private Rider rider;

    @Column(name = "riderid", insertable = false, updatable = false)
    private UUID riderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartid", nullable = false)
    private Cart cart;

    @Column(name = "cartid", insertable = false, updatable = false)
    private UUID cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerid", nullable = false)
    private Customer customer;

    @Column(name = "customerid", insertable = false, updatable = false)
    private UUID customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurantid", nullable = false)
    private Restaurant restaurant;

    @Column(name = "restaurantid", insertable = false, updatable = false)
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum NotificationStatus {
        PENDING,
        ACCEPTED,
        DECLINED,
        EXPIRED
    }
}
