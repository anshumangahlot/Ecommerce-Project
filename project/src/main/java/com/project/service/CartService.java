package com.project.service;

import com.project.repository.CartItemRepository;
import com.project.repository.CartRepository;
import com.project.repository.ProductRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.project.entity.Cart;
import com.project.entity.CartItem;
import com.project.entity.Product;
import com.project.entity.User;

@Service
@RequiredArgsConstructor
public class CartService {

        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final ProductRepository productRepository;
        private final UserRepository userRepository;

        public Cart addToCart(Long userId, Long productId, Integer quantity) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (quantity <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }

            if (product.getStock() < quantity) {
                throw new RuntimeException("Not enough stock available");
            }

            Cart cart = cartRepository.findByUser(user)
                    .orElseGet(() -> {
                        Cart newCart = new Cart();
                        newCart.setUser(user);
                        return cartRepository.save(newCart);
                    });

            CartItem cartItem = cartItemRepository
                    .findByCartAndProduct(cart, product)
                    .orElse(null);

            if (cartItem != null) {

                int newQuantity = cartItem.getQuantity() + quantity;

                if (newQuantity > product.getStock()) {
                    throw new RuntimeException("Requested quantity exceeds available stock");
                }

                cartItem.setQuantity(newQuantity);

            } else {

                cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(quantity);
            }

            cartItemRepository.save(cartItem);

            return cart;
        }

        public Cart getCart(Long userId) {

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return cartRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
        }

        public CartItem updateCartItem(Long cartItemId, Integer quantity) {

            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            if (quantity <= 0) {
                throw new RuntimeException("Quantity must be greater than 0");
            }

            Product product = cartItem.getProduct();

            if (quantity > product.getStock()) {
                throw new RuntimeException("Requested quantity exceeds available stock");
            }

            cartItem.setQuantity(quantity);

            return cartItemRepository.save(cartItem);
        }

        public void removeCartItem(Long cartItemId) {

            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            cartItemRepository.delete(cartItem);
        }
}
