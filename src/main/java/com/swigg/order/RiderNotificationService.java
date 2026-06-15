package com.swigg.order;

import com.swigg.cart.Cart;
import com.swigg.cart.CartRepository;
import com.swigg.cart.CartStatus;
import com.swigg.customer.Customer;
import com.swigg.customer.CustomerRepository;
import com.swigg.order.messaging.RiderNotificationMessage;
import com.swigg.restaurant.Restaurant;
import com.swigg.restaurant.RestaurantRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RiderNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(RiderNotificationService.class);

    @Autowired
    private RiderNotificationRepository riderNotificationRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.rider.notification.exchange:rider.notification.exchange}")
    private String riderNotificationExchange;

    @Value("${rider.notification.search.radius.km:5.0}")
    private double searchRadiusKm;

    @Transactional
    @CacheEvict(value = "riderNotifications", allEntries = true)
    public void sendNotificationsToNearbyRiders(UUID cartId) {
        logger.info("Sending notifications to nearby riders for cart: {}", cartId);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> {
                    logger.warn("Cart not found: {}", cartId);
                    return new IllegalArgumentException("Cart not found");
                });

        if (!cart.getStatus().equals(CartStatus.ORDERED)) {
            logger.warn("Cart is not in ORDERED status: {}", cart.getStatus());
            throw new IllegalArgumentException("Cart must be in ORDERED status");
        }

        Restaurant restaurant = restaurantRepository.findById(cart.getRestaurantId())
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found: {}", cart.getRestaurantId());
                    return new IllegalArgumentException("Restaurant not found");
                });

        Customer customer = customerRepository.findById(cart.getCustomerId())
                .orElseThrow(() -> {
                    logger.warn("Customer not found: {}", cart.getCustomerId());
                    return new IllegalArgumentException("Customer not found");
                });

        List<Rider> nearbyRiders = findNearbyRiders(restaurant.getLat(), restaurant.getLng());
        logger.info("Found {} nearby riders for cart: {}", nearbyRiders.size(), cartId);

        for (Rider rider : nearbyRiders) {
            RiderNotification notification = RiderNotification.builder()
                    .rider(rider)
                    .riderId(rider.getRiderId())
                    .cart(cart)
                    .cartId(cartId)
                    .customer(customer)
                    .customerId(cart.getCustomerId())
                    .restaurant(restaurant)
                    .restaurantId(cart.getRestaurantId())
                    .status(RiderNotification.NotificationStatus.PENDING)
                    .isActive(true)
                    .build();

            RiderNotification savedNotification = riderNotificationRepository.save(notification);
            logger.info("Created notification: {} for rider: {} cart: {}", savedNotification.getNotificationId(), rider.getRiderId(), cartId);

            publishRiderNotificationEvent(savedNotification, restaurant, customer);
        }
    }

    @Transactional
    @CacheEvict(value = "riderNotifications", allEntries = true)
    public RiderNotificationResponseDTO acceptNotification(UUID notificationId, UUID riderId) {
        logger.info("Rider: {} accepting notification: {}", riderId, notificationId);

        RiderNotification notification = riderNotificationRepository.findByNotificationIdAndRiderId(notificationId, riderId)
                .orElseThrow(() -> {
                    logger.warn("Notification not found for rider: {} notification: {}", riderId, notificationId);
                    return new IllegalArgumentException("Notification not found");
                });

        if (!notification.getStatus().equals(RiderNotification.NotificationStatus.PENDING)) {
            logger.warn("Notification is not in PENDING status: {}", notification.getStatus());
            throw new IllegalArgumentException("Notification is not in PENDING status");
        }

        notification.setStatus(RiderNotification.NotificationStatus.ACCEPTED);
        RiderNotification updatedNotification = riderNotificationRepository.save(notification);
        logger.info("Notification accepted: {} by rider: {}", notificationId, riderId);

        declineOtherNotificationsForCart(notification.getCartId(), riderId);

        return mapToResponseDTO(updatedNotification);
    }

    @Transactional
    @CacheEvict(value = "riderNotifications", allEntries = true)
    public RiderNotificationResponseDTO declineNotification(UUID notificationId, UUID riderId) {
        logger.info("Rider: {} declining notification: {}", riderId, notificationId);

        RiderNotification notification = riderNotificationRepository.findByNotificationIdAndRiderId(notificationId, riderId)
                .orElseThrow(() -> {
                    logger.warn("Notification not found for rider: {} notification: {}", riderId, notificationId);
                    return new IllegalArgumentException("Notification not found");
                });

        if (!notification.getStatus().equals(RiderNotification.NotificationStatus.PENDING)) {
            logger.warn("Notification is not in PENDING status: {}", notification.getStatus());
            throw new IllegalArgumentException("Notification is not in PENDING status");
        }

        notification.setStatus(RiderNotification.NotificationStatus.DECLINED);
        RiderNotification updatedNotification = riderNotificationRepository.save(notification);
        logger.info("Notification declined: {} by rider: {}", notificationId, riderId);

        return mapToResponseDTO(updatedNotification);
    }

    @Cacheable(value = "riderNotifications", key = "'rider_pending_' + #riderId")
    public List<RiderNotificationResponseDTO> getPendingNotificationsForRider(UUID riderId) {
        logger.info("Fetching pending notifications for rider: {}", riderId);

        List<RiderNotification> notifications = riderNotificationRepository.findPendingNotificationsByRiderId(riderId);
        logger.info("Found {} pending notifications for rider: {}", notifications.size(), riderId);

        return notifications.stream().map(this::mapToResponseDTO).toList();
    }

    @Cacheable(value = "riderNotifications", key = "'cart_notifications_' + #cartId")
    public List<RiderNotificationResponseDTO> getNotificationsForCart(UUID cartId) {
        logger.info("Fetching notifications for cart: {}", cartId);

        List<RiderNotification> notifications = riderNotificationRepository.findPendingNotificationsByCartId(cartId);
        logger.info("Found {} notifications for cart: {}", notifications.size(), cartId);

        return notifications.stream().map(this::mapToResponseDTO).toList();
    }

    private List<Rider> findNearbyRiders(BigDecimal restaurantLat, BigDecimal restaurantLng) {
        List<Rider> allRiders = riderRepository.findAll();
        return allRiders.stream()
                .filter(rider -> Boolean.TRUE.equals(rider.getIsActive()) && Boolean.TRUE.equals(rider.getIsVerified()))
                .filter(rider -> calculateDistance(restaurantLat, restaurantLng, rider.getLat(), rider.getLng()) <= searchRadiusKm)
                .toList();
    }

    private double calculateDistance(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        final int R = 6371;

        double latDistance = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double lngDistance = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private void declineOtherNotificationsForCart(UUID cartId, UUID acceptedRiderId) {
        List<RiderNotification> pendingNotifications = riderNotificationRepository.findPendingNotificationsByCartId(cartId);
        for (RiderNotification notification : pendingNotifications) {
            if (!notification.getRiderId().equals(acceptedRiderId)) {
                notification.setStatus(RiderNotification.NotificationStatus.DECLINED);
                riderNotificationRepository.save(notification);
                logger.info("Auto-declined notification: {} for rider: {} cart: {}", notification.getNotificationId(), notification.getRiderId(), cartId);
            }
        }
    }

    private void publishRiderNotificationEvent(RiderNotification notification, Restaurant restaurant, Customer customer) {
        try {
            RiderNotificationMessage message = RiderNotificationMessage.builder()
                    .notificationId(notification.getNotificationId())
                    .riderId(notification.getRiderId())
                    .cartId(notification.getCartId())
                    .customerId(notification.getCustomerId())
                    .restaurantId(notification.getRestaurantId())
                    .restaurantLat(restaurant.getLat())
                    .restaurantLng(restaurant.getLng())
                    .deliveryLat(customer.getLat())
                    .deliveryLng(customer.getLng())
                    .deliveryAddress(customer.getAddress())
                    .createdAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(riderNotificationExchange, "", message);
            logger.info("Published rider notification event for notification: {}", notification.getNotificationId());
        } catch (Exception e) {
            logger.error("Failed to publish rider notification event for notification: {}", notification.getNotificationId(), e);
        }
    }

    private RiderNotificationResponseDTO mapToResponseDTO(RiderNotification notification) {
        return RiderNotificationResponseDTO.builder()
                .notificationId(notification.getNotificationId())
                .riderId(notification.getRiderId())
                .cartId(notification.getCartId())
                .customerId(notification.getCustomerId())
                .restaurantId(notification.getRestaurantId())
                .status(notification.getStatus())
                .isActive(notification.getIsActive())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
