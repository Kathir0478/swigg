package com.swigg.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryOTPRepository extends JpaRepository<DeliveryOTP, UUID> {

    Optional<DeliveryOTP> findByOrderIdAndIsVerified(UUID orderId, Boolean isVerified);

    Optional<DeliveryOTP> findByOrderIdAndCodeAndIsVerified(UUID orderId, String code, Boolean isVerified);

    @Query("SELECT dto FROM DeliveryOTP dto WHERE dto.orderId = :orderId AND dto.isVerified = false AND dto.expiresAt > :now")
    Optional<DeliveryOTP> findValidOTPByOrderId(@Param("orderId") UUID orderId, @Param("now") LocalDateTime now);

    @Query("SELECT dto FROM DeliveryOTP dto WHERE dto.orderId = :orderId AND dto.code = :code AND dto.isVerified = false AND dto.expiresAt > :now")
    Optional<DeliveryOTP> findValidOTPByOrderIdAndCode(@Param("orderId") UUID orderId, @Param("code") String code, @Param("now") LocalDateTime now);
}
