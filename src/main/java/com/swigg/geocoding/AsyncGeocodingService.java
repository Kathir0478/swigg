package com.swigg.geocoding;

import com.swigg.cache.CacheService;
import com.swigg.customer.CustomerRepository;
import com.swigg.restaurant.RestaurantRepository;
import com.swigg.rider.RiderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AsyncGeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncGeocodingService.class);

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private CacheService cacheService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    @Lazy
    private AsyncGeocodingService self;

    @Value("${cache.geocoding-ttl:3600}")
    private long geocodingCacheTtl;

    public ReverseGeocodingResponseDTO reverseGeocodeWithCache(BigDecimal lat, BigDecimal lng) {
        String cacheKey = "geocoding:" + lat + ":" + lng;

        return cacheService.getOrElse(
                cacheKey,
                ReverseGeocodingResponseDTO.class,
                () -> {
                    logger.debug("Cache MISS for geocoding: {}, {}", lat, lng);
                    return geocodingService.reverseGeocode(lat, lng);
                },
                geocodingCacheTtl
        );
    }

    public void scheduleAddressUpdate(BigDecimal lat, BigDecimal lng, UUID userId, String entityType) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    self.updateAddressAsync(lat, lng, userId, entityType);
                }
            });
        } else {
            self.updateAddressAsync(lat, lng, userId, entityType);
        }
    }

    @Async
    @Transactional
    public void updateAddressAsync(BigDecimal lat, BigDecimal lng, UUID userId, String entityType) {
        try {
            logger.info("Starting async address lookup and update for user: {} of type: {}", userId, entityType);
            ReverseGeocodingResponseDTO lookup = reverseGeocodeWithCache(lat, lng);
            String address = lookup.getAddress();

            if (address == null || address.isBlank()) {
                logger.warn("Reverse geocoding returned empty address for coordinates: {}, {}", lat, lng);
                return;
            }

            if ("CUSTOMER".equalsIgnoreCase(entityType)) {
                customerRepository.findByUserId(userId).ifPresent(customer -> {
                    customer.setAddress(address);
                    customerRepository.save(customer);
                    logger.info("Successfully updated Customer address asynchronously in DB: {}", address);
                });
            } else if ("RESTAURANT".equalsIgnoreCase(entityType)) {
                restaurantRepository.findByUserId(userId).ifPresent(restaurant -> {
                    restaurant.setAddress(address);
                    restaurantRepository.save(restaurant);
                    logger.info("Successfully updated Restaurant address asynchronously in DB: {}", address);
                });
            } else if ("RIDER".equalsIgnoreCase(entityType)) {
                riderRepository.findByUserId(userId).ifPresent(rider -> {
                    rider.setAddress(address);
                    riderRepository.save(rider);
                    logger.info("Successfully updated Rider address asynchronously in DB: {}", address);
                });
            } else {
                logger.warn("Unknown entity type for async geocoding: {}", entityType);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Geocoding validation failed for user: {} of type: {}. Error: {}", userId, entityType, e.getMessage());
        } catch (Exception e) {
            logger.error("Error in async address update for user: {} of type: {}. Error: {}", userId, entityType, e.getMessage(), e);
        }
    }

    public void invalidateGeocodeCache(BigDecimal lat, BigDecimal lng) {
        String cacheKey = "geocoding:" + lat + ":" + lng;
        cacheService.delete(cacheKey);
        logger.info("Invalidated geocoding cache for coordinates: {}, {}", lat, lng);
    }
}
