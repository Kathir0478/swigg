package com.swigg.customer;

import com.swigg.auth.AuthService;
import com.swigg.auth.AsyncOtpService;
import com.swigg.auth.TokenResponseDTO;
import com.swigg.auth.TotpService;
import com.swigg.geocoding.AsyncGeocodingService;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private AsyncGeocodingService asyncGeocodingService;

    @Autowired
    private CustomerRepository customerRepository;

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

    @Transactional
    public CustomerInitResponseDTO initiateRegister(UUID userId, CustomerRegisterRequestDTO request) {
        logger.info("Customer registration initiated for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer registration failed: user '{}' not found", userId);
                    return new IllegalArgumentException("User not found");
                });

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            logger.warn("Customer registration failed: account deactivated for user '{}'", userId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (request.getLat() == null || request.getLng() == null) {
            logger.warn("Customer registration failed: latitude and longitude are required");
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        if (!geocodingService.validateCoordinates(request.getLat(), request.getLng())) {
            logger.warn("Customer registration failed: invalid coordinates for user '{}'", userId);
            throw new IllegalArgumentException("Invalid latitude or longitude coordinates");
        }
        Customer existingCustomer = customerRepository.findByUserId(userId).orElse(null);
        if (existingCustomer == null) {
            Customer customer = Customer.builder()
                    .userId(userId)
                    .user(user)
                    .name(user.getUserName())
                    .dob(request.getDob())
                    .gender(request.getGender())
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .isActive(true)
                    .isVerified(false)
                    .build();

            customerRepository.save(customer);
            asyncGeocodingService.scheduleAddressUpdate(request.getLat(), request.getLng(), userId, "CUSTOMER");
            logger.info("Customer created in database for userId: {} with isVerified=false", userId);
        }

        asyncOtpService.generateAndSendOtpAsync(userId.toString(), user.getPhoneNumber(), "CUSTOMER_REGISTER");
        logger.info("Customer registration OTP send initiated asynchronously for userId: {}", userId);

        String maskedPhone = OtpService.maskPhoneNumber(user.getPhoneNumber());
        logger.info("Customer registration verification code generation initiated for userId: {}. Awaiting verification.", userId);

        return new CustomerInitResponseDTO(
                "Verification code sent to your mobile number",
                maskedPhone
        );
    }

    @Transactional
    public Customer completeRegister(UUID userId, String totpCode) {
        logger.info("Customer registration verification started for userId: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer registration verification failed: customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found. Please register first.");
                });

        if (Boolean.TRUE.equals(customer.getIsVerified())) {
            logger.warn("Customer registration verification failed: customer already verified for userId: {}", userId);
            throw new IllegalArgumentException("Customer is already verified");
        }

        User user = customer.getUser();

        logger.info("Verifying TOTP code for customer registration: userId={}", userId);
        if (!totpService.verifyTotp(user.getTotpSecret(), totpCode, System.currentTimeMillis())) {
            logger.warn("Customer registration verification failed: invalid TOTP code for userId: {}", userId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        customer.setIsVerified(true);
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        Customer updatedCustomer = customerRepository.save(customer);
        evictCustomerCache(updatedCustomer.getCustomerId());
        logger.info("Customer verified successfully for userId: {} with isVerified=true and role=CUSTOMER", userId);

        return updatedCustomer;
    }

    public CustomerInitResponseDTO initiateLogin(CustomerLoginRequestDTO request) {
        logger.info("Customer login initiated for username: {}", request.getUsername());

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            logger.warn("Customer login failed: username is required");
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            logger.warn("Customer login failed: password is required");
            throw new IllegalArgumentException("Password is required");
        }

        Customer customer = customerRepository.findByUser_PhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> {
                    logger.warn("Customer login failed: customer '{}' not found", request.getUsername());
                    return new IllegalArgumentException("Invalid username or password");
                });

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Customer login failed: account deactivated for customer '{}'", request.getUsername());
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(customer.getIsVerified())) {
            logger.warn("Customer login failed: account not verified for customer '{}'", request.getUsername());
            throw new IllegalArgumentException("Customer is not verified");
        }

        User user = customer.getUser();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Customer login failed: incorrect password for customer '{}'", request.getUsername());
            throw new IllegalArgumentException("Invalid username or password");
        }

        asyncOtpService.generateAndSendOtpAsync(user.getUserId().toString(), user.getPhoneNumber(), "CUSTOMER_LOGIN");
        logger.info("Customer login OTP send initiated asynchronously for username: {}", request.getUsername());

        logger.info("Customer login TOTP code generation initiated for username: {}. Awaiting verification.", request.getUsername());
        return new CustomerInitResponseDTO(
                "Verification code sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    public TokenResponseDTO completeLogin(UUID customerId, String totpCode) {
        logger.info("Customer login verification started for customerId: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.warn("Customer login verification failed: customer not found for customerId: {}", customerId);
                    return new IllegalArgumentException("Customer not found");
                });

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Customer login verification failed: account deactivated for customerId: {}", customerId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (!Boolean.TRUE.equals(customer.getIsVerified())) {
            logger.warn("Customer login verification failed: account not verified for customerId: {}", customerId);
            throw new IllegalArgumentException("Customer is not verified");
        }

        User user = customer.getUser();
        logger.info("Verifying TOTP code for customer login: customerId={}", customerId);
        if (!totpService.verifyTotp(user.getTotpSecret(), totpCode, System.currentTimeMillis())) {
            logger.warn("Customer login verification failed: invalid TOTP code for customerId: {}", customerId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        logger.info("Customer login successful for username: {}", user.getUserName());
        return authService.generateTokensForUser(user);
    }

    public OtpSentResponseDTO requestDeletion(UUID userId) {
        logger.info("Customer deletion OTP requested for customerId: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Deletion OTP request failed: customer '{}' not found", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Deletion OTP request failed: customer '{}' is already deactivated", userId);
            throw new IllegalArgumentException("Account is already deactivated");
        }

        User user = customer.getUser();
        asyncOtpService.generateAndSendOtpAsync(userId.toString(), user.getPhoneNumber(), "CUSTOMER_DELETE");
        logger.info("Customer deletion OTP send initiated asynchronously for customerId: {}", userId);

        return new OtpSentResponseDTO(
                "Verification code sent to your mobile number",
                OtpService.maskPhoneNumber(user.getPhoneNumber())
        );
    }

    @Transactional
    public void completeDelete(UUID userId, CustomerDeleteVerifyDTO request) {
        logger.info("Customer deletion requested for customerId: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Deletion failed: customer '{}' not found", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Deletion failed: customer '{}' is already deactivated", userId);
            throw new IllegalArgumentException("Account is already deactivated");
        }

        User user = customer.getUser();
        logger.info("Verifying TOTP code for customer deletion: customerId={}", userId);
        if (!totpService.verifyTotp(user.getTotpSecret(), request.getOtpCode(), System.currentTimeMillis())) {
            logger.warn("Deletion failed: invalid TOTP code for customerId '{}'", userId);
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        customer.setIsActive(false);
        user.setRole(Role.USER);
        userRepository.save(user);
        customerRepository.save(customer);
        evictCustomerCache(customer.getCustomerId());
        logger.info("Customer deactivated successfully for customerId: {} and role changed back to USER", userId);
    }

    @Transactional
    public Customer updateCustomer(UUID userId, CustomerUpdateRequestDTO request) {
        logger.info("Customer update requested for userId: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer update failed: customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Customer update failed: customer is deactivated for userId: {}", userId);
            throw new IllegalArgumentException("Account is deactivated");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            customer.setName(request.getName());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
        if (request.getDob() != null) {
            customer.setDob(request.getDob());
        }
        if (request.getGender() != null) {
            customer.setGender(request.getGender());
        }
        boolean coordinatesChanged = false;
        if (request.getLat() != null) {
            customer.setLat(request.getLat());
            coordinatesChanged = true;
        }
        if (request.getLng() != null) {
            customer.setLng(request.getLng());
            coordinatesChanged = true;
        }

        Customer updatedCustomer = customerRepository.save(customer);
        evictCustomerCache(updatedCustomer.getCustomerId());
        if (coordinatesChanged) {
            asyncGeocodingService.scheduleAddressUpdate(customer.getLat(), customer.getLng(), userId, "CUSTOMER");
        }
        logger.info("Customer updated successfully for userId: {}", userId);
        return updatedCustomer;
    }

    @Cacheable(value = "customers", key = "'all'")
    public List<Customer> listAllCustomers() {
        logger.info("Fetching all active customers");
        List<Customer> customers = customerRepository.findAll();
        logger.info("Found {} customers", customers.size());
        return customers;
    }

    @Cacheable(value = "customer", key = "#customerId")
    public Customer getCustomerById(UUID customerId) {
        logger.info("Fetching customer for customerId: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for customerId: {}", customerId);
                    return new IllegalArgumentException("Customer not found");
                });

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Customer is deactivated for customerId: {}", customerId);
            throw new IllegalArgumentException("Customer is not available");
        }

        logger.info("Successfully fetched customer for customerId: {}", customerId);
        return customer;
    }

    private void evictCustomerCache(UUID customerId) {
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache listCache = cacheManager.getCache("customers");
                if (listCache != null) {
                    listCache.clear();
                }
                org.springframework.cache.Cache detailCache = cacheManager.getCache("customer");
                if (detailCache != null && customerId != null) {
                    detailCache.evict(customerId);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to evict customer cache for ID: {}", customerId, e);
        }
    }
}
