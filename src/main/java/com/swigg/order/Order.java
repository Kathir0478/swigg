package com.swigg.order;

import com.swigg.cart.Cart;
import com.swigg.customer.Customer;
import com.swigg.restaurant.Restaurant;
import com.swigg.rider.Rider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
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
        @Index(name = "idx_orders_customerid_isactive", columnList = "customerid,isactive"),
        @Index(name = "idx_orders_riderid_isactive", columnList = "riderid,isactive"),
        @Index(name = "idx_orders_restaurantid_isactive", columnList = "restaurantid,isactive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order implements Serializable {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartid", nullable = false)
    private Cart cart;

    @Column(name = "cartid", insertable = false, updatable = false)
    private UUID cartId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(columnDefinition = "TEXT")
    private String instruction;

    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
