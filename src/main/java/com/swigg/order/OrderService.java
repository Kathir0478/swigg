package com.swigg.order;

import com.swigg.cart.Cart;
import com.swigg.cart.CartRepository;
import com.swigg.cart.CartStatus;
import com.swigg.customer.Customer;
import com.swigg.customer.CustomerRepository;
import com.swigg.order.messaging.OrderStatusMessage;
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

import java.time.LocalDateTime;
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
    private CacheManager cacheManager;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.order.status.exchange:order.status.exchange}")
    private String orderStatusExchange;

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDTO createOrder(UUID customerId, OrderRequestDTO request) {
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
            logger.warn("Cart does not belong to customer: {}", customerId);
            throw new IllegalArgumentException("Cart does not belong to this customer");
        }

        if (!cart.getStatus().equals(CartStatus.ORDERED)) {
            logger.warn("Cart status is not ORDERED: {}", cart.getStatus());
            throw new IllegalArgumentException("Cart must be in ORDERED status to create an order");
        }

        Restaurant restaurant = restaurantRepository.findById(cart.getRestaurantId())
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found: {}", cart.getRestaurantId());
                    return new IllegalArgumentException("Restaurant not found");
                });

        Order order = Order.builder()
                .customer(customer)
                .customerId(customerId)
                .restaurant(restaurant)
                .restaurantId(cart.getRestaurantId())
                .cart(cart)
                .cartId(request.getCartId())
                .deliveryLat(request.getDeliveryLat())
                .deliveryLng(request.getDeliveryLng())
                .deliveryAddress(request.getDeliveryAddress())
                .customerInstructions(request.getCustomerInstructions())
                .status(OrderStatus.PENDING)
                .isActive(true)
                .build();

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully: {} for customer: {}", savedOrder.getOrderId(), customerId);

        publishOrderStatusEvent(savedOrder);

        return mapToResponseDTO(savedOrder);
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

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            logger.warn("Order is not in PENDING status: {}", order.getStatus());
            throw new IllegalArgumentException("Order is not in PENDING status");
        }

        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider not found: {}", riderId);
                    return new IllegalArgumentException("Rider not found");
                });

        if (!Boolean.TRUE.equals(rider.getIsActive()) || !Boolean.TRUE.equals(rider.getIsVerified())) {
            logger.warn("Rider is not active or verified: {}", riderId);
            throw new IllegalArgumentException("Rider is not available");
        }

        order.setRider(rider);
        order.setRiderId(riderId);
        order.setStatus(OrderStatus.ACCEPTED);

        Order updatedOrder = orderRepository.save(order);
        logger.info("Rider assigned successfully: {} to order: {}", riderId, orderId);

        publishOrderStatusEvent(updatedOrder);

        return mapToResponseDTO(updatedOrder);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDTO updateOrderStatus(UUID orderId, OrderStatus status) {
        logger.info("Updating order: {} status to: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found: {}", orderId);
                    return new IllegalArgumentException("Order not found");
                });

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order status updated successfully: {} to {}", orderId, status);

        publishOrderStatusEvent(updatedOrder);

        return mapToResponseDTO(updatedOrder);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public void cancelOrder(UUID orderId, UUID customerId) {
        logger.info("Cancelling order: {} by customer: {}", orderId, customerId);

        Order order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> {
                    logger.warn("Order not found for customer: {} order: {}", customerId, orderId);
                    return new IllegalArgumentException("Order not found");
                });

        if (!order.getStatus().equals(OrderStatus.PENDING) && !order.getStatus().equals(OrderStatus.ACCEPTED)) {
            logger.warn("Order cannot be cancelled in status: {}", order.getStatus());
            throw new IllegalArgumentException("Order cannot be cancelled in current status");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setIsActive(false);
        orderRepository.save(order);
        logger.info("Order cancelled successfully: {}", orderId);

        publishOrderStatusEvent(order);
    }

    @Cacheable(value = "orders", key = "'order_' + #orderId")
    public OrderResponseDTO getOrderById(UUID orderId) {
        logger.info("Fetching order: {}", orderId);

        Order order = orderRepository.findByOrderIdAndIsActive(orderId, true)
                .orElseThrow(() -> {
                    logger.warn("Order not found: {}", orderId);
                    return new IllegalArgumentException("Order not found");
                });

        logger.info("Successfully fetched order: {}", orderId);
        return mapToResponseDTO(order);
    }

    @Cacheable(value = "orders", key = "'customer_orders_' + #customerId")
    public List<OrderResponseDTO> getOrdersByCustomer(UUID customerId) {
        logger.info("Fetching orders for customer: {}", customerId);

        List<Order> orders = orderRepository.findByCustomerIdAndIsActive(customerId, true);
        logger.info("Successfully fetched {} orders for customer: {}", orders.size(), customerId);

        return orders.stream().map(this::mapToResponseDTO).toList();
    }

    @Cacheable(value = "orders", key = "'rider_orders_' + #riderId")
    public List<OrderResponseDTO> getOrdersByRider(UUID riderId) {
        logger.info("Fetching orders for rider: {}", riderId);

        List<Order> orders = orderRepository.findByRiderIdAndIsActive(riderId, true);
        logger.info("Successfully fetched {} orders for rider: {}", orders.size(), riderId);

        return orders.stream().map(this::mapToResponseDTO).toList();
    }

    private void publishOrderStatusEvent(Order order) {
        try {
            OrderStatusMessage message = OrderStatusMessage.builder()
                    .orderId(order.getOrderId())
                    .customerId(order.getCustomerId())
                    .riderId(order.getRiderId())
                    .restaurantId(order.getRestaurantId())
                    .status(order.getStatus())
                    .timestamp(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(orderStatusExchange, "", message);
            logger.info("Published order status event for order: {}", order.getOrderId());
        } catch (Exception e) {
            logger.error("Failed to publish order status event for order: {}", order.getOrderId(), e);
        }
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .riderId(order.getRiderId())
                .restaurantId(order.getRestaurantId())
                .cartId(order.getCartId())
                .deliveryLat(order.getDeliveryLat())
                .deliveryLng(order.getDeliveryLng())
                .deliveryAddress(order.getDeliveryAddress())
                .status(order.getStatus())
                .customerInstructions(order.getCustomerInstructions())
                .isActive(order.getIsActive())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
