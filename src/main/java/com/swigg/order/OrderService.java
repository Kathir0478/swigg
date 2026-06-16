package com.swigg.order;

import com.swigg.cart.Cart;
import com.swigg.cart.CartRepository;
import com.swigg.cart.CartStatus;
import com.swigg.customer.Customer;
import com.swigg.customer.CustomerRepository;
import com.swigg.food.Food;
import com.swigg.food.FoodRepository;
import com.swigg.restaurant.Restaurant;
import com.swigg.restaurant.RestaurantRepository;
import com.swigg.rider.Rider;
import com.swigg.rider.RiderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CacheManager cacheManager;

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDTO createOrder(UUID customerId, CreateOrderRequestDTO request) {
        logger.info("Creating order for customer: {} with cart: {}", customerId, request.getCartId());

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found: {}", customerId);
                    return new IllegalArgumentException("Customer not found");
                });

        if (!Boolean.TRUE.equals(customer.getIsActive())) {
            logger.warn("Customer account deactivated: {}", customerId);
            throw new IllegalArgumentException("Customer account is deactivated");
        }

        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> {
                    logger.warn("Cart not found: {}", request.getCartId());
                    return new IllegalArgumentException("Cart not found");
                });

        if (!cart.getCustomerId().equals(customerId)) {
            logger.warn("Cart does not belong to customer: {} for cart: {}", customerId, request.getCartId());
            throw new IllegalArgumentException("Cart does not belong to this customer");
        }

        if (!Boolean.TRUE.equals(cart.getIsActive())) {
            logger.warn("Cart is inactive: {}", request.getCartId());
            throw new IllegalArgumentException("Cart is inactive");
        }

        if (!cart.getStatus().equals(CartStatus.ORDERED)) {
            logger.warn("Cart status is not ORDERED: {} with status: {}", request.getCartId(), cart.getStatus());
            throw new IllegalArgumentException("Cart must be in ORDERED status to create an order");
        }

        if (cart.getFoodIds() == null || cart.getFoodIds().isEmpty()) {
            logger.warn("Cannot create order with empty cart: {}", request.getCartId());
            throw new IllegalArgumentException("Cannot create order with empty cart");
        }

        validateFoodAvailability(cart.getFoodIds());

        Restaurant restaurant = restaurantRepository.findById(cart.getRestaurantId())
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found: {}", cart.getRestaurantId());
                    return new IllegalArgumentException("Restaurant not found");
                });

        if (!Boolean.TRUE.equals(restaurant.getIsActive())) {
            logger.warn("Restaurant is not active: {}", cart.getRestaurantId());
            throw new IllegalArgumentException("Restaurant is not available");
        }

        Order order = Order.builder()
                .customer(customer)
                .customerId(customerId)
                .restaurant(restaurant)
                .restaurantId(cart.getRestaurantId())
                .cart(cart)
                .cartId(request.getCartId())
                .status(OrderStatus.PENDING)
                .instruction(request.getInstruction())
                .isActive(true)
                .build();

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully: {} for customer: {}", savedOrder.getOrderId(), customerId);

        return mapToResponseDTO(savedOrder);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDTO updateOrder(UUID customerId, UUID orderId, UpdateOrderRequestDTO request) {
        logger.info("Updating order: {} for customer: {}", orderId, customerId);

        Order order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> {
                    logger.warn("Order not found: {} for customer: {}", orderId, customerId);
                    return new IllegalArgumentException("Order not found or does not belong to this customer");
                });

        if (!Boolean.TRUE.equals(order.getIsActive())) {
            logger.warn("Cannot update inactive order: {}", orderId);
            throw new IllegalArgumentException("Order is no longer active");
        }

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        if (request.getInstruction() != null) {
            order.setInstruction(request.getInstruction());
        }

        Order updatedOrder = orderRepository.save(order);
        logger.info("Order updated successfully: {}", orderId);

        return mapToResponseDTO(updatedOrder);
    }

    @Cacheable(value = "orders", key = "'order_' + #orderId")
    public OrderResponseDTO getOrderById(UUID orderId) {
        logger.info("Fetching order: {}", orderId);

        Order order = orderRepository.findByOrderIdAndIsActive(orderId, true)
                .orElseThrow(() -> {
                    logger.warn("Order not found or inactive: {}", orderId);
                    return new IllegalArgumentException("Order not found");
                });

        logger.info("Successfully fetched order: {}", orderId);
        return mapToResponseDTO(order);
    }

    @Cacheable(value = "orders", key = "'customer_orders_' + #customerId")
    public List<OrderResponseDTO> getOrdersByCustomer(UUID customerId) {
        logger.info("Fetching orders for customer: {}", customerId);

        List<Order> orders = orderRepository.findByCustomerIdAndIsActive(customerId, true);
        logger.info("Found {} orders for customer: {}", orders.size(), customerId);

        return orders.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Cacheable(value = "orders", key = "'rider_orders_' + #riderId")
    public List<OrderResponseDTO> getOrdersByRider(UUID riderId) {
        logger.info("Fetching orders for rider: {}", riderId);

        List<Order> orders = orderRepository.findByRiderIdAndIsActive(riderId, true);
        logger.info("Found {} orders for rider: {}", orders.size(), riderId);

        return orders.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Cacheable(value = "orders", key = "'restaurant_orders_' + #restaurantId")
    public List<OrderResponseDTO> getOrdersByRestaurant(UUID restaurantId) {
        logger.info("Fetching orders for restaurant: {}", restaurantId);

        List<Order> orders = orderRepository.findByRestaurantIdAndIsActive(restaurantId, true);
        logger.info("Found {} orders for restaurant: {}", orders.size(), restaurantId);

        return orders.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDTO assignRider(UUID orderId, UUID riderId) {
        logger.info("Assigning rider: {} to order: {}", riderId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found: {}", orderId);
                    return new IllegalArgumentException("Order not found");
                });

        if (!Boolean.TRUE.equals(order.getIsActive())) {
            logger.warn("Cannot assign rider to inactive order: {}", orderId);
            throw new IllegalArgumentException("Order is no longer active");
        }

        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider not found: {}", riderId);
                    return new IllegalArgumentException("Rider not found");
                });

        if (!Boolean.TRUE.equals(rider.getIsActive())) {
            logger.warn("Rider is not active: {}", riderId);
            throw new IllegalArgumentException("Rider is not available");
        }

        order.setRider(rider);
        order.setRiderId(riderId);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        Order updatedOrder = orderRepository.save(order);
        logger.info("Rider assigned successfully: {} to order: {}", riderId, orderId);

        return mapToResponseDTO(updatedOrder);
    }

    private void validateFoodAvailability(List<UUID> foodIds) {
        logger.info("Validating food availability for {} items", foodIds.size());

        for (UUID foodId : foodIds) {
            Food food = foodRepository.findById(foodId)
                    .orElseThrow(() -> {
                        logger.warn("Food not found: {}", foodId);
                        return new IllegalArgumentException("Food not found: " + foodId);
                    });

            if (!Boolean.TRUE.equals(food.getIsActive())) {
                logger.warn("Food is inactive: {}", foodId);
                throw new IllegalArgumentException("Food is no longer available: " + food.getName());
            }

            if (!Boolean.TRUE.equals(food.getIsAvailable())) {
                logger.warn("Food is not available: {}", foodId);
                throw new IllegalArgumentException("Food is currently unavailable: " + food.getName());
            }
        }

        logger.info("All foods are available");
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .riderId(order.getRiderId())
                .restaurantId(order.getRestaurantId())
                .cartId(order.getCartId())
                .status(order.getStatus())
                .instruction(order.getInstruction())
                .isActive(order.getIsActive())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private void evictOrderCache(UUID orderId) {
        try {
            if (cacheManager != null) {
                org.springframework.cache.Cache cache = cacheManager.getCache("orders");
                if (cache != null) {
                    cache.evict("order_" + orderId);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to evict order cache for ID: {}", orderId, e);
        }
    }
}
