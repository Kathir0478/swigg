package com.swigg.order;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_otps", indexes = {
        @Index(name = "idx_delivery_otps_orderid", columnList = "orderid"),
        @Index(name = "idx_delivery_otps_code", columnList = "code"),
        @Index(name = "idx_delivery_otps_isverified", columnList = "isverified"),
        @Index(name = "idx_delivery_otps_expiresat", columnList = "expiresat")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID otpId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderid", nullable = false)
    private Order order;

    @Column(name = "orderid", insertable = false, updatable = false)
    private UUID orderId;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "isverified", nullable = false)
    private Boolean isVerified;

    @Column(name = "expiresat", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verifiedat")
    private LocalDateTime verifiedAt;

    @CreationTimestamp
    @Column(name = "createdat", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
