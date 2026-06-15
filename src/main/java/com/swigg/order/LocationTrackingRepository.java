package com.swigg.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationTrackingRepository extends JpaRepository<LocationTracking, UUID> {

    List<LocationTracking> findByOrderId(UUID orderId);

    List<LocationTracking> findByRiderId(UUID riderId);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.orderId = :orderId ORDER BY lt.timestamp DESC")
    List<LocationTracking> findByOrderIdOrderByTimestampDesc(@Param("orderId") UUID orderId);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.riderId = :riderId ORDER BY lt.timestamp DESC")
    List<LocationTracking> findByRiderIdOrderByTimestampDesc(@Param("riderId") UUID riderId);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.orderId = :orderId AND lt.timestamp >= :since ORDER BY lt.timestamp DESC")
    List<LocationTracking> findByOrderIdAndTimestampAfter(@Param("orderId") UUID orderId, @Param("since") LocalDateTime since);

    @Query("SELECT lt FROM LocationTracking lt WHERE lt.orderId = :orderId ORDER BY lt.timestamp DESC LIMIT 1")
    Optional<LocationTracking> findLatestByOrderId(@Param("orderId") UUID orderId);
}
