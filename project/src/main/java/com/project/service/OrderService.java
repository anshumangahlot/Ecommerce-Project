package com.project.service;

import com.project.dto.OrderItemResponse;
import com.project.dto.OrderResponse;
import com.project.entity.Cart;
import com.project.entity.CartItem;
import com.project.entity.Order;
import com.project.entity.OrderItem;
import com.project.entity.Product;
import com.project.entity.User;
import com.project.exception.BadRequestException;
import com.project.exception.ResourceNotFoundException;
import com.project.repository.CartRepository;
import com.project.repository.OrderRepository;
import com.project.repository.ProductRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse placeOrder() {

        // 1. Find user
        User user = getAuthenticatedUser();

        // 2. Find user's cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        // 3. Check cart is not empty
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place order with an empty cart");
        }

        // 4. Create Order
        Order order = new Order();
        order.setUser(user);
//        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CONFIRMED");

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 5. Convert CartItems → OrderItems
        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            // 6. Check stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName()
                );
            }

            // 7. Create OrderItem
            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            // Store price at purchase time
            orderItem.setPrice(product.getPrice());

            // 8. Calculate item total
            BigDecimal itemTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            totalAmount = totalAmount.add(itemTotal);

            // Add item to Order
            order.getItems().add(orderItem);

            // 9. Reduce stock
            product.setStock(
                    product.getStock() - cartItem.getQuantity()
            );

            productRepository.save(product);
        }

        // 10. Set total
        order.setTotalAmount(totalAmount);

        // 11. Save Order + OrderItems through cascade
        Order savedOrder = orderRepository.save(order);

        // 12. Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return convertToOrderResponse(savedOrder);
    }


    public OrderResponse getOrderById(Long orderId) {
        User user = getAuthenticatedUser();
        Order order=  orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found with id: " + orderId)
                );
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(
                    "You are not allowed to access this order"
            );
        }

        return convertToOrderResponse(order);
    }

    public List<OrderResponse> getMyOrders() {
        User user = getAuthenticatedUser();
        return orderRepository.findByUser(user)
                .stream()
                .map(this::convertToOrderResponse)
                .toList();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

//    public List<OrderResponse> getUserOrders(Long userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() ->
//                        new RuntimeException("User not found with id: " + userId)
//                );
//        return orderRepository.findByUser(user)
//                .stream().map(this::convertToOrderResponse)
//                .toList();
//    }

    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();

        response.setId(orderItem.getId());
        response.setProductId(orderItem.getProduct().getId());
        response.setQuantity(orderItem.getQuantity());
        response.setPrice(orderItem.getPrice());

        return response;
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();

        response.setOrderId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::convertToOrderItemResponse)
                .toList();

        response.setItems(items);
        return response;
    }
}
