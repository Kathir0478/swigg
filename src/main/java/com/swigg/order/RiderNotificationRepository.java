package com.swigg.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiderNotificationRepository extends JpaRepository<RiderNotification, UUID> {

    Optional<RiderNotification> findByNotificationIdAndRiderId(UUID notificationId, UUID riderId);

    Optional<RiderNotification> findByCartIdAndRiderIdAndStatus(UUID cartId, UUID riderId, RiderNotification.NotificationStatus status);

    List<RiderNotification> findByRiderIdAndStatusAndIsActive(UUID riderId, RiderNotification.NotificationStatus status, Boolean isActive);

    List<RiderNotification> findByCartIdAndStatus(UUID cartId, RiderNotification.NotificationStatus status);

    @Query("SELECT rn FROM RiderNotification rn WHERE rn.cartId = :cartId AND rn.status = 'PENDING' AND rn.isActive = true")
    List<RiderNotification> findPendingNotificationsByCartId(@Param("cartId") UUID cartId);

    @Query("SELECT rn FROM RiderNotification rn WHERE rn.riderId = :riderId AND rn.status = 'PENDING' AND rn.isActive = true")
    List<RiderNotification> findPendingNotificationsByRiderId(@Param("riderId") UUID riderId);
}
