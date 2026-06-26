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
import java.util.Optional;
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
    private CartItemRepository cartItemRepository;

    @Autowired
    private CacheManager cacheManager;

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponseDTO createOrGetCart(UUID userId, CartRequestDTO request) {
        logger.info("Create or get cart for userId: {} in restaurant: {}", userId, request.getRestaurantId());

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

        // Check if active cart exists with correct state (not ordered)
        Optional<Cart> existingCart = cartRepository.findActiveCartByCustomerAndRestaurant(customerId, request.getRestaurantId());
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            // Validate cart state - only return if not ordered
            if (cart.getStatus().equals(CartStatus.ORDERED)) {
                logger.warn("Existing cart is in ORDERED state for userId: {} in restaurant: {}", userId, request.getRestaurantId());
                // Cart is ordered, so create new one
            } else {
                logger.info("Active cart found for userId: {} in restaurant: {}. Returning existing cart.", userId, request.getRestaurantId());
                return mapToResponseDTO(cart);
            }
        }

        // Create new cart
        Cart cart = Cart.builder()
                .customer(customer)
                .customerId(customerId)
                .restaurant(restaurant)
                .restaurantId(request.getRestaurantId())
                .cartItems(new ArrayList<>())
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
    public CartResponseDTO addFoodToCart(UUID userId, UUID cartId, AddFoodToCartRequestDTO request) {
        logger.info("Adding food to cart: {} for userId: {} with foodId: {}", cartId, userId, request.getFoodId());

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
            logger.warn("Cannot add food to inactive cart: {}", cartId);
            throw new IllegalArgumentException("Cart is no longer active");
        }

        if (!cart.getStatus().equals(CartStatus.ACTIVE)) {
            logger.warn("Cannot add food to non-active status cart: {} with status: {}", cartId, cart.getStatus());
            throw new IllegalArgumentException("Cannot add food to cart with status: " + cart.getStatus());
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

        // Check if food item already exists in cart
        Optional<CartItem> existingCartItem = cartItemRepository.findByCartIdAndFoodId(cartId, request.getFoodId());

        if (existingCartItem.isPresent()) {
            // Update quantity if exists
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItemRepository.save(cartItem);
            logger.info("Updated quantity for foodId: {} in cart: {}", request.getFoodId(), cartId);
        } else {
            // Add new cart item
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .cartId(cartId)
                    .food(food)
                    .foodId(request.getFoodId())
                    .quantity(1)
                    .build();
            cartItemRepository.save(cartItem);
            logger.info("Added new food item to cart: {} with foodId: {}", cartId, request.getFoodId());
        }

        // Calculate total price using provided price
        cart.setTotalPrice(cart.getTotalPrice().add(BigDecimal.valueOf(request.getPrice())));
        Cart updatedCart = cartRepository.save(cart);
        
        int totalItems = calculateTotalItems(cartId);
        logger.info("Food added to cart: {}. New total items: {}, price: {}", cartId, totalItems, cart.getTotalPrice());

        return mapToResponseDTO(updatedCart);
    }

    @Transactional
    @CacheEvict(value = "carts", allEntries = true)
    public CartResponseDTO removeFoodFromCart(UUID userId, UUID cartId, RemoveFoodFromCartRequestDTO request) {
        logger.info("Removing food from cart: {} for userId: {} with foodId: {} count: {}", cartId, userId, request.getFoodId(), request.getCount());

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
            logger.warn("Cannot remove food from inactive cart: {}", cartId);
            throw new IllegalArgumentException("Cart is no longer active");
        }

        if (!cart.getStatus().equals(CartStatus.ACTIVE)) {
            logger.warn("Cannot remove food from non-active status cart: {}", cartId);
            throw new IllegalArgumentException("Cannot remove food from cart with status: " + cart.getStatus());
        }

        // Get cart item
        CartItem cartItem = cartItemRepository.findByCartIdAndFoodId(cartId, request.getFoodId())
                .orElseThrow(() -> {
                    logger.warn("Food not found in cart: {}", request.getFoodId());
                    return new IllegalArgumentException("Food not found in cart");
                });

        // Get food to calculate price deduction
        Food food = foodRepository.findById(request.getFoodId()).orElse(null);
        if (food == null) {
            logger.warn("Food not found: {}", request.getFoodId());
            throw new IllegalArgumentException("Food not found");
        }

        if (cartItem.getQuantity() <= request.getCount()) {
            // Remove entire cart item if count >= quantity
            cartItemRepository.delete(cartItem);
            logger.info("Removed entire food item from cart: {} with foodId: {}", cartId, request.getFoodId());
        } else {
            // Reduce quantity
            cartItem.setQuantity(cartItem.getQuantity() - request.getCount());
            cartItemRepository.save(cartItem);
            logger.info("Reduced quantity for foodId: {} in cart: {}", request.getFoodId(), cartId);
        }

        // Deduct price for removed items
        BigDecimal priceDeduction = BigDecimal.valueOf(food.getPrice()).multiply(BigDecimal.valueOf(request.getCount()));
        cart.setTotalPrice(cart.getTotalPrice().subtract(priceDeduction));
        
        Cart updatedCart = cartRepository.save(cart);
        int totalItems = calculateTotalItems(cartId);
        logger.info("Food removed from cart: {}. Remaining items: {}, price: {}", cartId, totalItems, cart.getTotalPrice());

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

    @Cacheable(value = "carts", key = "'customer_' + #customerId + '_restaurant_' + #restaurantId")
    public CartResponseDTO getActiveCartByCustomerAndRestaurant(UUID userId, UUID customerId, UUID restaurantId) {
        logger.info("Fetching active cart for customerId: {} and restaurantId: {}", customerId, restaurantId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        if (!customer.getCustomerId().equals(customerId)) {
            logger.warn("CustomerId mismatch for userId: {}", userId);
            throw new IllegalArgumentException("CustomerId does not match authenticated user");
        }

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Customer account deactivated for userId: {}", userId);
            throw new IllegalArgumentException("Customer account is deactivated");
        }

        Cart cart = cartRepository.findActiveCartByCustomerAndRestaurant(customerId, restaurantId)
                .orElseThrow(() -> {
                    logger.warn("No active cart found for customerId: {} and restaurantId: {}", customerId, restaurantId);
                    return new IllegalArgumentException("No active cart found for this restaurant");
                });

        if (!Boolean.TRUE.equals(cart.getIsActive())) {
            logger.warn("Cart is inactive for customerId: {} and restaurantId: {}", customerId, restaurantId);
            throw new IllegalArgumentException("Cart is no longer available");
        }

        if (!cart.getStatus().equals(CartStatus.ACTIVE)) {
            logger.warn("Cart status is not ACTIVE for customerId: {} and restaurantId: {}", customerId, restaurantId);
            throw new IllegalArgumentException("Cart is not in ACTIVE state");
        }

        logger.info("Successfully fetched active cart for customerId: {} and restaurantId: {}", customerId, restaurantId);
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

    @Cacheable(value = "carts", key = "'user_' + #userId + '_restaurant_' + #restaurantId + '_with_details'")
    public CartWithFoodDetailsResponseDTO searchCartWithFoodDetails(UUID userId, UUID restaurantId) {
        logger.info("Searching cart with food details for userId: {} and restaurantId: {}", userId, restaurantId);

        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found for userId: {}", userId);
                    return new IllegalArgumentException("Customer not found");
                });

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Customer account deactivated for userId: {}", userId);
            throw new IllegalArgumentException("Customer account is deactivated");
        }

        Cart cart = cartRepository.findActiveCartByCustomerAndRestaurant(customer.getCustomerId(), restaurantId)
                .orElseThrow(() -> {
                    logger.warn("No active cart found for userId: {} and restaurantId: {}", userId, restaurantId);
                    return new IllegalArgumentException("No active cart found for this restaurant");
                });

        if (!Boolean.TRUE.equals(cart.getIsActive())) {
            logger.warn("Cart is inactive for userId: {} and restaurantId: {}", userId, restaurantId);
            throw new IllegalArgumentException("Cart is no longer available");
        }

        if (!cart.getStatus().equals(CartStatus.ACTIVE)) {
            logger.warn("Cart status is not ACTIVE for userId: {} and restaurantId: {}", userId, restaurantId);
            throw new IllegalArgumentException("Cart is not in ACTIVE state");
        }

        // Fetch food details for all cart items
        List<CartWithFoodDetailsResponseDTO.FoodItemDetail> foodItemDetails = new java.util.ArrayList<>();
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());
        
        for (CartItem cartItem : cartItems) {
            Food food = cartItem.getFood();
            if (food != null) {
                CartWithFoodDetailsResponseDTO.FoodItemDetail detail = 
                        CartWithFoodDetailsResponseDTO.FoodItemDetail.builder()
                                .foodId(food.getFoodId())
                                .foodName(food.getFoodName())
                                .description(food.getDescription())
                                .price(food.getPrice())
                                .category(food.getCategory() != null ? food.getCategory().name() : null)
                                .quantity(cartItem.getQuantity())
                                .build();
                foodItemDetails.add(detail);
            }
        }

        CartWithFoodDetailsResponseDTO response = CartWithFoodDetailsResponseDTO.builder()
                .cartId(cart.getCartId())
                .customerId(cart.getCustomerId())
                .restaurantId(cart.getRestaurantId())
                .foodItems(foodItemDetails)
                .totalPrice(cart.getTotalPrice())
                .status(cart.getStatus())
                .isActive(cart.getIsActive())
                .itemCount(calculateTotalItems(cart.getCartId()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();

        logger.info("Successfully fetched cart with food details for userId: {} and restaurantId: {}", userId, restaurantId);
        return response;
    }


    private CartResponseDTO mapToResponseDTO(Cart cart) {
        // Fetch cart items with details
        List<CartResponseDTO.CartItemDetail> cartItemDetails = new ArrayList<>();
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());
        
        for (CartItem cartItem : cartItems) {
            CartResponseDTO.CartItemDetail detail = 
                    CartResponseDTO.CartItemDetail.builder()
                            .foodId(cartItem.getFoodId())
                            .quantity(cartItem.getQuantity())
                            .build();
            cartItemDetails.add(detail);
        }

        return CartResponseDTO.builder()
                .cartId(cart.getCartId())
                .customerId(cart.getCustomerId())
                .restaurantId(cart.getRestaurantId())
                .cartItems(cartItemDetails)
                .totalPrice(cart.getTotalPrice())
                .status(cart.getStatus())
                .isActive(cart.getIsActive())
                .itemCount(calculateTotalItems(cart.getCartId()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private int calculateTotalItems(UUID cartId) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
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
