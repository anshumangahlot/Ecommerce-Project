package com.project.service;

import com.project.entity.Cart;
import com.project.entity.CartItem;
import com.project.entity.Order;
import com.project.entity.OrderItem;
import com.project.entity.Product;
import com.project.entity.User;
import com.project.repository.CartRepository;
import com.project.repository.OrderRepository;
import com.project.repository.ProductRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    public Order placeOrder(Long userId) {

        // 1. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Find user's cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // 3. Check cart is not empty
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order with an empty cart");
        }

        // 4. Create Order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CONFIRMED");

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 5. Convert CartItems → OrderItems
        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            // 6. Check stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(
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

        return savedOrder;
    }


    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found with id: " + orderId)
                );
    }


    public List<Order> getUserOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + userId)
                );
        return orderRepository.findByUser(user);
    }
}
