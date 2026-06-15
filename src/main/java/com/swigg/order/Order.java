package com.swigg.order;

import com.swigg.cart.Cart;
import com.swigg.customer.Customer;
import com.swigg.restaurant.Restaurant;
import com.swigg.rider.Rider;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customerid", columnList = "customerid"),
        @Index(name = "idx_orders_riderid", columnList = "riderid"),
        @Index(name = "idx_orders_restaurantid", columnList = "restaurantid"),
        @Index(name = "idx_orders_cartid", columnList = "cartid"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_isactive", columnList = "isactive"),
        @Index(name = "idx_orders_customerid_status", columnList = "customerid,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customerid", nullable = false)
    private Customer customer;

    @Column(name = "customerid", insertable = false, updatable = false)
    private UUID customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "riderid")
    private Rider rider;

    @Column(name = "riderid", insertable = false, updatable = false)
    private UUID riderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurantid", nullable = false)
    private Restaurant restaurant;

    @Column(name = "restaurantid", insertable = false, updatable = false)
    private UUID restaurantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartid", nullable = false)
    private Cart cart;

    @Column(name = "cartid", insertable = false, updatable = false)
    private UUID cartId;

    @NotNull(message = "Delivery latitude is required")
    @DecimalMin(value = "-90.000000", message = "Latitude out of bounds")
    @DecimalMax(value = "90.000000", message = "Latitude out of bounds")
    @Column(name = "delivery_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal deliveryLat;

    @NotNull(message = "Delivery longitude is required")
    @DecimalMin(value = "-180.000000", message = "Longitude out of bounds")
    @DecimalMax(value = "180.000000", message = "Longitude out of bounds")
    @Column(name = "delivery_lng", nullable = false, precision = 11, scale = 6)
    private BigDecimal deliveryLng;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "customer_instructions", columnDefinition = "TEXT")
    private String customerInstructions;

    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
