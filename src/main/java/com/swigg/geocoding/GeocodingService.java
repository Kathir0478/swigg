package com.swigg.geocoding;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    @Value("${google.maps.api-key:}")
    private String apiKey;

    public GeocodingResponseDTO geocode(String address) {
        logger.info("Geocoding address: {}", address);

        if (address == null || address.isBlank()) {
            logger.warn("Geocoding failed: address is required");
            throw new IllegalArgumentException("Address is required");
        }

        if (apiKey == null || apiKey.isBlank() || "your-google-maps-api-key-here".equalsIgnoreCase(apiKey)) {
            logger.warn("Geocoding API key not set or placeholder. Falling back to mock coordinates.");
            return new GeocodingResponseDTO(
                    BigDecimal.valueOf(13.0827),
                    BigDecimal.valueOf(80.2707),
                    address,
                    "Address geocoded via fallback"
            );
        }

        try {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();

            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

            if (results == null || results.length == 0) {
                logger.warn("Geocoding failed: no results found for address: {}", address);
                throw new IllegalArgumentException("Address not found");
            }

            LatLng location = results[0].geometry.location;
            String formattedAddress = results[0].formattedAddress;

            logger.info("Geocoding successful for address: {} -> Lat: {}, Lng: {}",
                    address, location.lat, location.lng);

            return new GeocodingResponseDTO(
                    BigDecimal.valueOf(location.lat),
                    BigDecimal.valueOf(location.lng),
                    formattedAddress,
                    "Address geocoded successfully"
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Geocoding error for address: {}. Falling back to mock coordinates.", address, e);
            return new GeocodingResponseDTO(
                    BigDecimal.valueOf(13.0827),
                    BigDecimal.valueOf(80.2707),
                    address,
                    "Address geocoded via fallback due to error: " + e.getMessage()
            );
        }
    }

    public ReverseGeocodingResponseDTO reverseGeocode(BigDecimal lat, BigDecimal lng) {
        logger.info("Reverse geocoding coordinates: Lat: {}, Lng: {}", lat, lng);

        if (lat == null || lng == null) {
            logger.warn("Reverse geocoding failed: latitude and longitude are required");
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        if (lat.compareTo(new BigDecimal("-90")) < 0 || lat.compareTo(new BigDecimal("90")) > 0) {
            logger.warn("Reverse geocoding failed: latitude out of bounds");
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }

        if (lng.compareTo(new BigDecimal("-180")) < 0 || lng.compareTo(new BigDecimal("180")) > 0) {
            logger.warn("Reverse geocoding failed: longitude out of bounds");
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        if (apiKey == null || apiKey.isBlank() || "your-google-maps-api-key-here".equalsIgnoreCase(apiKey)) {
            logger.warn("Reverse geocoding API key not set or placeholder. Falling back to mock address.");
            return new ReverseGeocodingResponseDTO(
                    "Mock Address (Lat: " + lat + ", Lng: " + lng + ")",
                    "Coordinates reverse geocoded via fallback"
            );
        }

        try {
            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();

            GeocodingResult[] results = GeocodingApi.reverseGeocode(context,
                    new LatLng(lat.doubleValue(), lng.doubleValue())).await();

            if (results == null || results.length == 0) {
                logger.warn("Reverse geocoding failed: no results found for coordinates Lat: {}, Lng: {}", lat, lng);
                throw new IllegalArgumentException("Location not found");
            }

            String formattedAddress = results[0].formattedAddress;
            logger.info("Reverse geocoding successful for coordinates Lat: {}, Lng: {} -> Address: {}",
                    lat, lng, formattedAddress);

            return new ReverseGeocodingResponseDTO(
                    formattedAddress,
                    "Coordinates reverse geocoded successfully"
            );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Reverse geocoding error for coordinates Lat: {}, Lng: {}. Falling back to mock address.", lat, lng, e);
            return new ReverseGeocodingResponseDTO(
                    "Mock Address (Lat: " + lat + ", Lng: " + lng + ")",
                    "Coordinates reverse geocoded via fallback due to error: " + e.getMessage()
            );
        }
    }

    public boolean validateCoordinates(BigDecimal lat, BigDecimal lng) {
        if (lat == null || lng == null) {
            return false;
        }

        return lat.compareTo(new BigDecimal("-90")) >= 0
                && lat.compareTo(new BigDecimal("90")) <= 0
                && lng.compareTo(new BigDecimal("-180")) >= 0
                && lng.compareTo(new BigDecimal("180")) <= 0;
    }
}
