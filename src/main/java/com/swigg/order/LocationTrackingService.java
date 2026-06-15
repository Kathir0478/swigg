package com.swigg.order;

import com.swigg.order.LocationTracking;
import com.swigg.order.Order;
import com.swigg.order.OrderStatus;
import com.swigg.order.messaging.LocationTrackingMessage;
import com.swigg.rider.Rider;
import com.swigg.rider.RiderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocationTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(LocationTrackingService.class);

    @Autowired
    private LocationTrackingRepository locationTrackingRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.location.tracking.exchange:location.tracking.exchange}")
    private String locationTrackingExchange;

    @Transactional
    @CacheEvict(value = "locationTracking", allEntries = true)
    public LocationTrackingResponseDTO updateLocation(LocationTrackingRequestDTO request) {
        logger.info("Updating location for order: {} rider: {}", request.getOrderId(), request.getRiderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    logger.warn("Order not found: {}", request.getOrderId());
                    return new IllegalArgumentException("Order not found");
                });

        if (!order.getStatus().equals(OrderStatus.ACCEPTED) && 
            !order.getStatus().equals(OrderStatus.PICKED_UP) && 
            !order.getStatus().equals(OrderStatus.IN_TRANSIT)) {
            logger.warn("Order is not in a trackable status: {}", order.getStatus());
            throw new IllegalArgumentException("Order is not in a trackable status");
        }

        Rider rider = riderRepository.findById(request.getRiderId())
                .orElseThrow(() -> {
                    logger.warn("Rider not found: {}", request.getRiderId());
                    return new IllegalArgumentException("Rider not found");
                });

        if (!order.getRiderId().equals(request.getRiderId())) {
            logger.warn("Rider: {} is not assigned to order: {}", request.getRiderId(), request.getOrderId());
            throw new IllegalArgumentException("Rider is not assigned to this order");
        }

        LocationTracking tracking = LocationTracking.builder()
                .order(order)
                .orderId(request.getOrderId())
                .rider(rider)
                .riderId(request.getRiderId())
                .lat(request.getLat())
                .lng(request.getLng())
                .accuracy(request.getAccuracy())
                .speed(request.getSpeed())
                .heading(request.getHeading())
                .build();

        LocationTracking savedTracking = locationTrackingRepository.save(tracking);
        logger.info("Location updated successfully for order: {} rider: {}", request.getOrderId(), request.getRiderId());

        publishLocationTrackingEvent(savedTracking, order.getCustomerId());

        return mapToResponseDTO(savedTracking);
    }

    @Cacheable(value = "locationTracking", key = "'order_locations_' + #orderId")
    public List<LocationTrackingResponseDTO> getLocationsByOrder(UUID orderId) {
        logger.info("Fetching location history for order: {}", orderId);

        List<LocationTracking> locations = locationTrackingRepository.findByOrderIdOrderByTimestampDesc(orderId);
        logger.info("Found {} location updates for order: {}", locations.size(), orderId);

        return locations.stream().map(this::mapToResponseDTO).toList();
    }

    @Cacheable(value = "locationTracking", key = "'rider_locations_' + #riderId")
    public List<LocationTrackingResponseDTO> getLocationsByRider(UUID riderId) {
        logger.info("Fetching location history for rider: {}", riderId);

        List<LocationTracking> locations = locationTrackingRepository.findByRiderIdOrderByTimestampDesc(riderId);
        logger.info("Found {} location updates for rider: {}", locations.size(), riderId);

        return locations.stream().map(this::mapToResponseDTO).toList();
    }

    @Cacheable(value = "locationTracking", key = "'latest_location_' + #orderId")
    public LocationTrackingResponseDTO getLatestLocationByOrder(UUID orderId) {
        logger.info("Fetching latest location for order: {}", orderId);

        LocationTracking tracking = locationTrackingRepository.findLatestByOrderId(orderId)
                .orElseThrow(() -> {
                    logger.warn("No location data found for order: {}", orderId);
                    return new IllegalArgumentException("No location data found for this order");
                });

        logger.info("Successfully fetched latest location for order: {}", orderId);
        return mapToResponseDTO(tracking);
    }

    @Cacheable(value = "locationTracking", key = "'order_locations_since_' + #orderId + '_' + #since")
    public List<LocationTrackingResponseDTO> getLocationsByOrderSince(UUID orderId, LocalDateTime since) {
        logger.info("Fetching location history for order: {} since: {}", orderId, since);

        List<LocationTracking> locations = locationTrackingRepository.findByOrderIdAndTimestampAfter(orderId, since);
        logger.info("Found {} location updates for order: {} since: {}", locations.size(), orderId, since);

        return locations.stream().map(this::mapToResponseDTO).toList();
    }

    private void publishLocationTrackingEvent(LocationTracking tracking, UUID customerId) {
        try {
            LocationTrackingMessage message = LocationTrackingMessage.builder()
                    .trackingId(tracking.getTrackingId())
                    .orderId(tracking.getOrderId())
                    .riderId(tracking.getRiderId())
                    .customerId(customerId)
                    .lat(tracking.getLat())
                    .lng(tracking.getLng())
                    .accuracy(tracking.getAccuracy())
                    .speed(tracking.getSpeed())
                    .heading(tracking.getHeading())
                    .timestamp(tracking.getTimestamp())
                    .build();

            rabbitTemplate.convertAndSend(locationTrackingExchange, "", message);
            logger.info("Published location tracking event for order: {}", tracking.getOrderId());
        } catch (Exception e) {
            logger.error("Failed to publish location tracking event for order: {}", tracking.getOrderId(), e);
        }
    }

    private LocationTrackingResponseDTO mapToResponseDTO(LocationTracking tracking) {
        return LocationTrackingResponseDTO.builder()
                .trackingId(tracking.getTrackingId())
                .orderId(tracking.getOrderId())
                .riderId(tracking.getRiderId())
                .lat(tracking.getLat())
                .lng(tracking.getLng())
                .accuracy(tracking.getAccuracy())
                .speed(tracking.getSpeed())
                .heading(tracking.getHeading())
                .timestamp(tracking.getTimestamp())
                .build();
    }
}
