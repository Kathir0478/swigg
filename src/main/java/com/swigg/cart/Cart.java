package com.swigg.cart;

import com.swigg.customer.Customer;
import com.swigg.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_carts_customerid", columnList = "customerid"),
        @Index(name = "idx_carts_restaurantid", columnList = "restaurantid"),
        @Index(name = "idx_carts_isactive", columnList = "isactive"),
        @Index(name = "idx_carts_status", columnList = "status"),
        @Index(name = "idx_carts_customerid_isactive", columnList = "customerid,isactive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    @ElementCollection(targetClass = UUID.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cartid"))
    @Column(name = "foodid")
    private List<UUID> foodIds;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartStatus status;

    @Column(nullable = false)
    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
