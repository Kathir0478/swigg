package com.swigg.restaurant;

import com.swigg.auth.AuthService;
import com.swigg.auth.TokenResponseDTO;
import com.swigg.auth.TotpService;
import com.swigg.geocoding.GeocodingService;
import com.swigg.geocoding.ReverseGeocodingResponseDTO;
import com.swigg.messaging.OtpSentResponseDTO;
import com.swigg.messaging.OtpService;
import com.swigg.user.Role;
import com.swigg.user.User;
import com.swigg.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;

    @Autowired
    private AuthService authService;


    @Transactional
    public RestaurantInitResponseDTO initiateRegister(UUID userId, RestaurantRegisterRequestDTO request) {
        logger.info("Restaurant registration initiated for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant registration failed: user '{}' not found", userId);
                    return new IllegalArgumentException("User not found");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Restaurant registration failed: account deactivated for user '{}'", userId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (restaurantRepository.findById(userId).isPresent()) {
            logger.warn("Restaurant registration failed: restaurant already exists for user '{}'", userId);
            throw new IllegalArgumentException("Restaurant already registered for this user");
        }

        if (request.getLat() == null || request.getLng() == null) {
            logger.warn("Restaurant registration failed: latitude and longitude are required");
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        if (!geocodingService.validateCoordinates(request.getLat(), request.getLng())) {
            logger.warn("Restaurant registration failed: invalid coordinates for user '{}'", userId);
            throw new IllegalArgumentException("Invalid latitude or longitude coordinates");
        }
        ReverseGeocodingResponseDTO address=geocodingService.reverseGeocode(request.getLat(),request.getLng());

        Restaurant existingRestaurant = restaurantRepository.findByUserId(userId).orElse(null);
        if (existingRestaurant==null){
            Restaurant restaurant = Restaurant.builder()
                    .userId(userId)
                    .user(user)
                    .name(user.getUserName())
                    .description(request.getDescription())
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .address(address.getAddress())
                    .isActive(true)
                    .isVerified(false)
                    .build();

            populateAddressFromCoordinates(restaurant);
            restaurantRepository.save(restaurant);
            logger.info("Restaurant created in database for userId: {} with isVerified=false", userId);
        }

        String totpCode = totpService.generateTotp(user.getTotpSecret(), System.currentTimeMillis());
        logger.warn("RESTAURANT REGISTER TOTP CODE FOR PHONE {}: {}", user.getPhoneNumber(), totpCode);

        String maskedPhone = OtpService.maskPhoneNumber(user.getPhoneNumber());
        logger.info("Restaurant registration verification code generated for userId: {}. Awaiting verification.", userId);

        return new RestaurantInitResponseDTO(
                "Verification code sent to your mobile number",
                maskedPhone
        );
    }

    @Transactional
    public Restaurant completeRegister(UUID userId, String totpCode) {
        logger.info("Restaurant registration verification started for userId: {}", userId);

        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant registration verification failed: restaurant not found for userId: {}", userId);
                    return new IllegalArgumentException("Restaurant not found. Please register first.");
                });

        if (Boolean.TRUE.equals(restaurant.getIsVerified())) {
            logger.warn("Restaurant registration verification failed: restaurant already verified for userId: {}", userId);
            throw new IllegalArgumentException("Restaurant is already verified");
        }

        User user = restaurant.getUser();

        logger.info("Verifying TOTP code for restaurant registration: userId={}", userId);
        if (!totpService.verifyTotp(user.getTotpSecret(), totpCode, System.currentTimeMillis())) {
            logger.warn("Restaurant registration verification failed: invalid TOTP code for userId: {}", userId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        restaurant.setIsVerified(true);
        user.setRole(Role.RESTAURANT);
        userRepository.save(user);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant verified successfully for userId: {} with isVerified=true and role=RESTAURANT", userId);

        return updatedRestaurant;
    }

    public RestaurantInitResponseDTO initiateLogin(String username, String password) {
        logger.info("Restaurant login initiated for username: {}", username);

        if (username == null || username.isBlank()) {
            logger.warn("Restaurant login failed: username is required");
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.isBlank()) {
            logger.warn("Restaurant login failed: password is required");
            throw new IllegalArgumentException("Password is required");
        }

        Restaurant restaurant = restaurantRepository.findByUser_UserName(username)
                .orElseThrow(() -> {
                    logger.warn("Restaurant login failed: restaurant '{}' not found", username);
                    return new IllegalArgumentException("Invalid username or password");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant login failed: account deactivated for restaurant '{}'", username);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(restaurant.getIsVerified())) {
            logger.warn("Restaurant login failed: account not verified for restaurant '{}'", username);
            throw new IllegalArgumentException("Restaurant is not verified");
        }

        User user = restaurant.getUser();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            logger.warn("Restaurant login failed: incorrect password for restaurant '{}'", username);
            throw new IllegalArgumentException("Invalid username or password");
        }

        String totpCode = totpService.generateTotp(user.getTotpSecret(), System.currentTimeMillis());
        logger.warn("RESTAURANT LOGIN TOTP CODE FOR PHONE {}: {}", user.getPhoneNumber(), totpCode);

        logger.info("Restaurant login TOTP code generated for username: {}. Awaiting verification.", username);
        return new RestaurantInitResponseDTO(
                "Verification code sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    public TokenResponseDTO completeLogin(UUID restaurantId, String totpCode) {
        logger.info("Restaurant login verification started for restaurantId: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant login verification failed: restaurant not found for restaurantId: {}", restaurantId);
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant login verification failed: account deactivated for restaurantId: {}", restaurantId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(restaurant.getIsVerified())) {
            logger.warn("Restaurant login verification failed: account not verified for restaurantId: {}", restaurantId);
            throw new IllegalArgumentException("Restaurant is not verified");
        }

        User user = restaurant.getUser();
        logger.info("Verifying TOTP code for restaurant login: restaurantId={}", restaurantId);
        if (!totpService.verifyTotp(user.getTotpSecret(), totpCode, System.currentTimeMillis())) {
            logger.warn("Restaurant login verification failed: invalid TOTP code for restaurantId: {}", restaurantId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        logger.info("Restaurant login successful for username: {}", user.getUserName());
        return authService.generateTokensForUser(user);
    }

    public OtpSentResponseDTO requestDeletion(UUID userId) {
        logger.info("Restaurant deletion OTP requested for restaurantId: {}", userId);

        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Deletion OTP request failed: restaurant '{}' not found", userId);
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Deletion OTP request failed: restaurant '{}' is already deactivated", userId);
            throw new IllegalArgumentException("Account is already deactivated");
        }

        User user = restaurant.getUser();
        String totpCode = totpService.generateTotp(user.getTotpSecret(), System.currentTimeMillis());
        logger.warn("RESTAURANT DELETE TOTP CODE FOR PHONE {}: {}", user.getPhoneNumber(), totpCode);

        return new OtpSentResponseDTO(
                "Verification code sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    @Transactional
    public void completeDelete(UUID userId, RestaurantDeleteVerifyDTO request) {
        logger.info("Restaurant deletion requested for restaurantId: {}", userId);

        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Deletion failed: restaurant '{}' not found", userId);
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Deletion failed: restaurant '{}' is already deactivated", userId);
            throw new IllegalArgumentException("Account is already deactivated");
        }

        User user = restaurant.getUser();
        logger.info("Verifying TOTP code for restaurant deletion: restaurantId={}", userId);
        if (!totpService.verifyTotp(user.getTotpSecret(), request.getOtpCode(), System.currentTimeMillis())) {
            logger.warn("Deletion failed: invalid TOTP code for restaurantId '{}'", userId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        restaurant.setIsActive(false);
        user.setRole(Role.USER);
        userRepository.save(user);
        restaurantRepository.save(restaurant);
        logger.info("Restaurant deactivated successfully for restaurantId: {} and role changed back to USER", userId);
    }

    @Transactional
    public Restaurant updateRestaurant(UUID restaurantId, RestaurantUpdateRequestDTO request) {
        logger.info("Restaurant update requested for restaurantId: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findByUserId(restaurantId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant update failed: restaurant '{}' not found", restaurantId);
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant update failed: restaurant '{}' is deactivated", restaurantId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            restaurant.setName(request.getName());
        }
        if (request.getDescription() != null) {
            restaurant.setDescription(request.getDescription());
        }
        if (request.getOpenTime() != null) {
            restaurant.setOpenTime(request.getOpenTime());
        }
        if (request.getCloseTime() != null) {
            restaurant.setCloseTime(request.getCloseTime());
        }
        if (request.getImageUrl() != null) {
            restaurant.setImageUrl(request.getImageUrl());
        }
        boolean addressChange=false;
        if (request.getLat() != null) {
            restaurant.setLat(request.getLat());
            addressChange=true;
        }
        if (request.getLng() != null) {
            restaurant.setLng(request.getLng());
            addressChange=true;
        }
        if (addressChange){
            ReverseGeocodingResponseDTO location=geocodingService.reverseGeocode(restaurant.getLat(),restaurant.getLng());
            restaurant.setAddress(location.getAddress());
        }
        populateAddressFromCoordinates(restaurant);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant updated successfully for restaurantId: {}", restaurantId);
        return updatedRestaurant;
    }

    public List<Restaurant> listAllRestaurants() {
        logger.info("Fetching all active restaurants");
        List<Restaurant> restaurants = restaurantRepository.findAll();
        logger.info("Found {} restaurants", restaurants.size());
        return restaurants;
    }

    public Restaurant getRestaurantById(UUID restaurantId) {
        logger.info("Fetching restaurant for restaurantId: {}", restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found for restaurantId: {}", restaurantId);
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant is deactivated for restaurantId: {}", restaurantId);
            throw new IllegalArgumentException("Restaurant is not available");
        }

        logger.info("Successfully fetched restaurant for restaurantId: {}", restaurantId);
        return restaurant;
    }

    private void populateAddressFromCoordinates(Restaurant restaurant) {
        if (restaurant.getLat() == null || restaurant.getLng() == null) {
            return;
        }

        if (restaurant.getAddress() != null && !restaurant.getAddress().isBlank()) {
            return;
        }

        try {
            ReverseGeocodingResponseDTO response = geocodingService.reverseGeocode(restaurant.getLat(), restaurant.getLng());
            restaurant.setAddress(response.getAddress());
            logger.info("Address populated via reverse geocoding for restaurant");
        } catch (Exception e) {
            logger.warn("Failed to reverse geocode coordinates. Continuing without address: {}", e.getMessage());
        }
    }
}
