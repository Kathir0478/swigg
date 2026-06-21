package com.swigg.cart;

import com.swigg.customer.Customer;
import com.swigg.customer.CustomerRepository;
import com.swigg.food.Food;
import com.swigg.food.FoodRepository;
import com.swigg.restaurant.Restaurant;
import com.swigg.restaurant.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CacheManager cacheManager;

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponseDTO createCart(UUID userId, CartRequestDTO request) {
        logger.info("Creating cart for userId: {} in restaurant: {}", userId, request.getRestaurantId());

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        UUID customerId = customer.getCustomerId();

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Customer account deactivated for userId: {}", userId);
            throw new IllegalArgumentException("Customer account is deactivated");
        }

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found: {}", request.getRestaurantId());
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant account deactivated: {}", request.getRestaurantId());
            throw new IllegalArgumentException("Restaurant is not available");
        }

        cartRepository.findActiveCartByCustomerAndRestaurant(customerId, request.getRestaurantId())
                .ifPresent(existingCart -> {
                    logger.warn("Active cart already exists for userId: {} in restaurant: {}", userId, request.getRestaurantId());
                    throw new IllegalArgumentException("Active cart already exists for this restaurant");
                });

        Cart cart = Cart.builder()
                .customer(customer)
                .customerId(customerId)
                .restaurant(restaurant)
                .restaurantId(request.getRestaurantId())
                .foodIds(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .status(CartStatus.ACTIVE)
                .isActive(true)
                .build();

        Cart savedCart = cartRepository.save(cart);
        logger.info("Cart created successfully: {} for userId: {}", savedCart.getCartId(), userId);

        return mapToResponseDTO(savedCart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponseDTO addItemToCart(UUID userId, UUID cartId, AddToCartRequestDTO request) {
        logger.info("Adding item to cart: {} for userId: {}", cartId, userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        UUID customerId = customer.getCustomerId();

        Cart cart = cartRepository.findByCartIdAndCustomerId(cartId, customerId)
                .orElseThrow(() -> {
                    logger.warn("Cart not found: {} for userId: {}", cartId, userId);
                    return new IllegalArgumentException("Cart not found or does not belong to this customer");
                });

        if (!Boolean.TRUE.equals(cart.getIsActive())) {
            logger.warn("Cannot add item to inactive cart: {}", cartId);
            throw new IllegalArgumentException("Cart is no longer active");
        }

        if (!cart.getStatus().equals(CartStatus.ACTIVE)) {
            logger.warn("Cannot add item to non-active status cart: {} with status: {}", cartId, cart.getStatus());
            throw new IllegalArgumentException("Cannot add items to cart with status: " + cart.getStatus());
        }

        Food food = foodRepository.findByFoodIdAndIsActive(request.getFoodId(), true)
                .orElseThrow(() -> {
                    logger.warn("Food not found or inactive: {}", request.getFoodId());
                    return new IllegalArgumentException("Food not found or unavailable");
                });

        if (!food.getRestaurantId().equals(cart.getRestaurantId())) {
            logger.warn("Food: {} does not belong to cart restaurant: {}", request.getFoodId(), cart.getRestaurantId());
            throw new IllegalArgumentException("Food does not belong to this restaurant");
        }

        if (!Boolean.TRUE.equals(food.getIsAvailable())) {
            logger.warn("Food is not available: {}", request.getFoodId());
            throw new IllegalArgumentException("Food is currently unavailable");
        }

        for (int i = 0; i < request.getQuantity(); i++) {
            cart.getFoodIds().add(request.getFoodId());
        }

        cart.setTotalPrice(calculateCartTotal(cart.getFoodIds()));
        Cart updatedCart = cartRepository.save(cart);
        logger.info("Item added to cart: {}. New total items: {}, price: {}", cartId, cart.getFoodIds().size(), cart.getTotalPrice());

        return mapToResponseDTO(updatedCart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponseDTO removeItemFromCart(UUID userId, UUID cartId, UUID foodId, Integer quantity) {
        logger.info("Removing item from cart: {} for userId: {}", cartId, userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        UUID customerId = customer.getCustomerId();

        Cart cart = cartRepository.findByCartIdAndCustomerId(cartId, customerId)
                .orElseThrow(() -> {
                    logger.warn("Cart not found: {} for userId: {}", cartId, userId);
                    return new IllegalArgumentException("Cart not found or does not belong to this customer");
                });

        if (!Boolean.TRUE.equals(cart.getIsActive())) {
            logger.warn("Cannot remove item from inactive cart: {}", cartId);
            throw new IllegalArgumentException("Cart is no longer active");
        }

        if (!cart.getStatus().equals(CartStatus.ACTIVE)) {
            logger.warn("Cannot remove item from non-active status cart: {}", cartId);
            throw new IllegalArgumentException("Cannot remove items from cart with status: " + cart.getStatus());
        }

        int itemCount = 0;
        for (int i = 0; i < quantity; i++) {
            if (cart.getFoodIds().remove(foodId)) {
                itemCount++;
            }
        }

        if (itemCount == 0) {
            logger.warn("Food not found in cart: {}", foodId);
            throw new IllegalArgumentException("Food not found in cart");
        }

        cart.setTotalPrice(calculateCartTotal(cart.getFoodIds()));
        Cart updatedCart = cartRepository.save(cart);
        logger.info("Items removed from cart: {}. Remaining items: {}, price: {}", cartId, cart.getFoodIds().size(), cart.getTotalPrice());

        return mapToResponseDTO(updatedCart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponseDTO placeOrder(UUID userId, UUID cartId) {
        logger.info("Placing order for cart: {} by userId: {}", cartId, userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        UUID customerId = customer.getCustomerId();

        Cart cart = cartRepository.findByCartIdAndCustomerId(cartId, customerId)
                .orElseThrow(() -> {
                    logger.warn("Cart not found: {} for userId: {}", cartId, userId);
                    return new IllegalArgumentException("Cart not found or does not belong to this customer");
                });

        if (!Boolean.TRUE.equals(cart.getIsActive())) {
            logger.warn("Cannot place order with inactive cart: {}", cartId);
            throw new IllegalArgumentException("Cart is no longer active");
        }

        if (cart.getFoodIds().isEmpty()) {
            logger.warn("Cannot place order with empty cart: {}", cartId);
            throw new IllegalArgumentException("Cannot place order with empty cart");
        }

        cart.setStatus(CartStatus.ORDERED);
        Cart updatedCart = cartRepository.save(cart);
        logger.info("Order placed successfully for cart: {} with {} items, total: {}", cartId, cart.getFoodIds().size(), cart.getTotalPrice());

        return mapToResponseDTO(updatedCart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public void deleteCart(UUID userId, UUID cartId) {
        logger.info("Deleting cart: {} for userId: {}", cartId, userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        UUID customerId = customer.getCustomerId();

        Cart cart = cartRepository.findByCartIdAndCustomerId(cartId, customerId)
                .orElseThrow(() -> {
                    logger.warn("Cart not found: {} for userId: {}", cartId, userId);
                    return new IllegalArgumentException("Cart not found or does not belong to this customer");
                });

        cart.setIsActive(false);
        cartRepository.save(cart);
        logger.info("Cart deleted (deactivated) successfully: {}", cartId);
    }

    @Cacheable(value = "carts", key = "'cart_' + #cartId")
    public CartResponseDTO getCartById(UUID userId, UUID cartId) {
        logger.info("Fetching cart: {} for userId: {}", cartId, userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        UUID customerId = customer.getCustomerId();

        Cart cart = cartRepository.findByCartIdAndCustomerId(cartId, customerId)
                .orElseThrow(() -> {
                    logger.warn("Cart not found: {} for userId: {}", cartId, userId);
                    return new IllegalArgumentException("Cart not found");
                });

        if (!Boolean.TRUE.equals(cart.getIsActive())) {
            logger.warn("Cart is inactive: {}", cartId);
            throw new IllegalArgumentException("Cart is no longer available");
        }

        logger.info("Successfully fetched cart: {}", cartId);
        return mapToResponseDTO(cart);
    }

    @Cacheable(value = "carts", key = "'customer_active_' + #userId")
    public CartResponseDTO getActiveCartByCustomer(UUID userId) {
        logger.info("Fetching active cart for userId: {}", userId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        UUID customerId = customer.getCustomerId();

        Cart cart = cartRepository.findByCustomerIdAndStatusAndIsActive(customerId, CartStatus.ACTIVE, true)
                .orElseThrow(() -> {
                    logger.warn("No active cart found for userId: {}", userId);
                    return new IllegalArgumentException("No active cart found");
                });

        logger.info("Successfully fetched active cart for userId: {}", userId);
        return mapToResponseDTO(cart);
    }

    private BigDecimal calculateCartTotal(List<UUID> foodIds) {
        BigDecimal total = BigDecimal.ZERO;

        for (UUID foodId : foodIds) {
            Food food = foodRepository.findById(foodId).orElse(null);
            if (food != null) {
                total = total.add(BigDecimal.valueOf(food.getPrice()));
            }
        }

        logger.debug("Calculated cart total: {}", total);
        return total;
    }

    private CartResponseDTO mapToResponseDTO(Cart cart) {
        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .customerId(cart.getCustomerId())
                .restaurantId(cart.getRestaurantId())
                .foodIds(cart.getFoodIds())
                .totalPrice(cart.getTotalPrice())
                .status(cart.getStatus())
                .isActive(cart.getIsActive())
                .itemCount(cart.getFoodIds() != null ? cart.getFoodIds().size() : 0)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private void evictCartCache(UUID cartId) {
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache("carts");
                if (cache != null) {
                    cache.evict("cart_" + cartId);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to evict cart cache for ID: {}", cartId, e);
        }
    }
}
