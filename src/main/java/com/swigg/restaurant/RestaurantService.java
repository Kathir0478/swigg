package com.swigg.restaurant;

import com.swigg.auth.AuthService;
import com.swigg.auth.AsyncOtpService;
import com.swigg.auth.TokenResponseDTO;
import com.swigg.auth.TotpService;
import com.swigg.geocoding.AsyncGeocodingService;
import com.swigg.filestorage.FileStorageService;
import com.swigg.geocoding.GeocodingService;
import com.swigg.messaging.OtpSentResponseDTO;
import com.swigg.messaging.OtpService;
import com.swigg.user.Role;
import com.swigg.user.User;
import com.swigg.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private AsyncGeocodingService asyncGeocodingService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TotpService totpService;

    @Autowired
    private AsyncOtpService asyncOtpService;

    @Autowired
    private AuthService authService;

    @Autowired
    private FileStorageService fileStorageService;


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
        Restaurant existingRestaurant = restaurantRepository.findByUserId(userId).orElse(null);
        if (existingRestaurant == null) {
            String imageUrl = null;
            if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
                imageUrl = fileStorageService.storeFile(request.getImageFile(), "restaurant");
            }

            // Parse ISO-8601 datetime strings to extract LocalTime
            LocalTime openTime = parseLocalTime(request.getOpenTime());
            LocalTime closeTime = parseLocalTime(request.getCloseTime());

            Restaurant restaurant = Restaurant.builder()
                    .userId(userId)
                    .user(user)
                    .name(user.getUserName())
                    .description(request.getDescription())
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .imageUrl(imageUrl)
                    .openTime(openTime)
                    .closeTime(closeTime)
                    .isActive(true)
                    .isVerified(false)
                    .build();

            restaurantRepository.save(restaurant);
            asyncGeocodingService.scheduleAddressUpdate(request.getLat(), request.getLng(), userId, "RESTAURANT");
            logger.info("Restaurant created in database for userId: {} with isVerified=false", userId);
        }

        asyncOtpService.generateAndSendOtpAsync(userId.toString(), user.getPhoneNumber(), "RESTAURANT_REGISTER");
        logger.info("Restaurant registration OTP send initiated asynchronously for userId: {}", userId);

        String maskedPhone = OtpService.maskPhoneNumber(user.getPhoneNumber());
        logger.info("Restaurant registration verification code generation initiated for userId: {}. Awaiting verification.", userId);

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
        evictRestaurantCache(updatedRestaurant.getRestaurantId());
        logger.info("Restaurant verified successfully for userId: {} with isVerified=true and role=RESTAURANT", userId);

        return updatedRestaurant;
    }

    public RestaurantInitResponseDTO initiateLogin(RestaurantLoginRequestDTO data) {
        logger.info("Restaurant login initiated for username: {}", data.getUsername());

        if (data.getUsername() == null || data.getUsername().isBlank()) {
            logger.warn("Restaurant login failed: username is required");
            throw new IllegalArgumentException("Username is required");
        }
        if (data.getPassword() == null || data.getPassword().isBlank()) {
            logger.warn("Restaurant login failed: password is required");
            throw new IllegalArgumentException("Password is required");
        }
        if (data.getPhoneNumber()==null || data.getPhoneNumber().isBlank()){
            logger.warn("Restaurant login failed: password is required");
            throw new IllegalArgumentException("Password is required");
        }

        Restaurant restaurant = restaurantRepository.findByUser_PhoneNumberAndUser_IsActive(data.getPhoneNumber(), true)
                .orElseThrow(() -> {
                    logger.warn("Restaurant login failed: active restaurant '{}' not found", data.getUsername());
                    return new IllegalArgumentException("Invalid username or password");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant login failed: account deactivated for restaurant '{}'", data.getUsername());
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(restaurant.getIsVerified())) {
            logger.warn("Restaurant login failed: account not verified for restaurant '{}'", data.getUsername());
            throw new IllegalArgumentException("Restaurant is not verified");
        }

        User user = restaurant.getUser();
        if (!passwordEncoder.matches(data.getPassword(), user.getPasswordHash())) {
            logger.warn("Restaurant login failed: incorrect password for restaurant '{}'", data.getUsername());
            throw new IllegalArgumentException("Invalid username or password");
        }

        asyncOtpService.generateAndSendOtpAsync(user.getUserId().toString(), user.getPhoneNumber(), "RESTAURANT_LOGIN");
        logger.info("Restaurant login OTP send initiated asynchronously for username: {}", data.getUsername());

        logger.info("Restaurant login TOTP code generation initiated for username: {}. Awaiting verification.", data.getUsername());
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
        asyncOtpService.generateAndSendOtpAsync(userId.toString(), user.getPhoneNumber(), "RESTAURANT_DELETE");
        logger.info("Restaurant deletion OTP send initiated asynchronously for restaurantId: {}", userId);

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
        evictRestaurantCache(restaurant.getRestaurantId());
        logger.info("Restaurant deactivated successfully for restaurantId: {} and role changed back to USER", userId);
    }

    @Transactional
    @CacheEvict(value = {"restaurant", "restaurants"}, allEntries = true)
    public Restaurant updateRestaurant(UUID userId, RestaurantUpdateRequestDTO request) {
        logger.info("Restaurant update requested for userId: {}", userId);
        logger.debug("Request payload: name={}, description={}, openTime={}, closeTime={}, lat={}, lng={}",
                request.getName(), request.getDescription(), request.getOpenTime(), request.getCloseTime(),
                request.getLat(), request.getLng());

        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant update failed: restaurant not found for userId: {}", userId);
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant update failed: restaurant is deactivated for userId: {}", userId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        // Preserve existing address to prevent overwriting with null
        String existingAddress = restaurant.getAddress();

        boolean anyFieldUpdated = false;

        if (request.getName() != null && !request.getName().isBlank()) {
            logger.info("Updating name from '{}' to '{}'", restaurant.getName(), request.getName());
            restaurant.setName(request.getName());
            anyFieldUpdated = true;
        }
        if (request.getDescription() != null) {
            logger.info("Updating description");
            restaurant.setDescription(request.getDescription());
            anyFieldUpdated = true;
        }
        if (request.getOpenTime() != null && !request.getOpenTime().isBlank()) {
            LocalTime openTime = parseLocalTime(request.getOpenTime());
            if (openTime != null) {
                logger.info("Updating openTime from '{}' to '{}'", restaurant.getOpenTime(), openTime);
                restaurant.setOpenTime(openTime);
                anyFieldUpdated = true;
            } else {
                logger.warn("Failed to parse openTime: {}", request.getOpenTime());
            }
        }
        if (request.getCloseTime() != null && !request.getCloseTime().isBlank()) {
            LocalTime closeTime = parseLocalTime(request.getCloseTime());
            if (closeTime != null) {
                logger.info("Updating closeTime from '{}' to '{}'", restaurant.getCloseTime(), closeTime);
                restaurant.setCloseTime(closeTime);
                anyFieldUpdated = true;
            } else {
                logger.warn("Failed to parse closeTime: {}", request.getCloseTime());
            }
        }
        boolean coordinatesChanged = false;
        if (request.getLat() != null) {
            logger.info("Updating lat from '{}' to '{}'", restaurant.getLat(), request.getLat());
            restaurant.setLat(request.getLat());
            coordinatesChanged = true;
            anyFieldUpdated = true;
        }
        if (request.getLng() != null) {
            logger.info("Updating lng from '{}' to '{}'", restaurant.getLng(), request.getLng());
            restaurant.setLng(request.getLng());
            coordinatesChanged = true;
            anyFieldUpdated = true;
        }

        if (!anyFieldUpdated) {
            logger.warn("No fields were updated for userId: {}", userId);
            throw new IllegalArgumentException("No fields provided for update");
        }

        // Always preserve the existing address to prevent overwriting
        restaurant.setAddress(existingAddress);
        logger.debug("Preserved existing address for restaurant: {}", existingAddress);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant saved to database for userId: {}", userId);
        if (coordinatesChanged) {
            asyncGeocodingService.scheduleAddressUpdate(restaurant.getLat(), restaurant.getLng(), userId, "RESTAURANT");
        }
        logger.info("Restaurant updated successfully for userId: {}", userId);
        return updatedRestaurant;
    }

    @Transactional
    @CacheEvict(value = {"restaurant", "restaurants"}, allEntries = true)
    public Restaurant updateRestaurantImage(UUID userId, MultipartFile imageFile) {
        logger.info("Restaurant image update requested for userId: {}", userId);

        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant image update failed: restaurant not found for userId: {}", userId);
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant image update failed: restaurant is deactivated for userId: {}", userId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (imageFile == null || imageFile.isEmpty()) {
            logger.warn("Restaurant image update failed: no image file provided for userId: {}", userId);
            throw new IllegalArgumentException("Image file is required");
        }

        // Delete old image if exists
        if (restaurant.getImageUrl() != null) {
            fileStorageService.deleteFile(restaurant.getImageUrl());
        }

        // Store new image
        String imageUrl = fileStorageService.storeFile(imageFile, "restaurant");
        restaurant.setImageUrl(imageUrl);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant image updated successfully for userId: {}", userId);
        return updatedRestaurant;
    }

    @Cacheable(value = "restaurants", key = "'all'")
    public List<Restaurant> listAllRestaurants() {
        logger.info("Fetching all active restaurants");
        List<Restaurant> restaurants = restaurantRepository.findAll();
        logger.info("Found {} restaurants", restaurants.size());
        return restaurants;
    }

    @Cacheable(value = "restaurant", key = "#restaurantId")
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

    @Cacheable(value = "restaurant", key = "#userId")
    public Restaurant getRestaurantByUserId(UUID userId) {
        logger.info("Fetching restaurant for userId: {}", userId);
        Restaurant restaurant = restaurantRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found for userId: {}", userId);
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant is deactivated for userId: {}", userId);
            throw new IllegalArgumentException("Restaurant is not available");
        }

        logger.info("Successfully fetched restaurant for userId: {}", userId);
        return restaurant;
    }

    private void evictRestaurantCache(UUID restaurantId) {
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache listCache = cacheManager.getCache("restaurants");
                if (listCache != null) {
                    listCache.clear();
                }
                org.springframework.cache.Cache detailCache = cacheManager.getCache("restaurant");
                if (detailCache != null && restaurantId != null) {
                    detailCache.evict(restaurantId);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to evict restaurant cache for ID: {}", restaurantId, e);
        }
    }

    private LocalTime parseLocalTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }
        try {
            return java.time.ZonedDateTime.parse(timeStr).toLocalTime();
        } catch (Exception e) {
            // ignore
        }
        try {
            return java.time.LocalDateTime.parse(timeStr).toLocalTime();
        } catch (Exception e) {
            // ignore
        }
        try {
            return java.time.OffsetDateTime.parse(timeStr).toLocalTime();
        } catch (Exception e) {
            // ignore
        }
        try {
            return java.time.LocalTime.parse(timeStr);
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
