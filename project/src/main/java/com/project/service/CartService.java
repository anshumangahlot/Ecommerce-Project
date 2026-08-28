package com.project.service;

import com.project.dto.CartItemResponse;
import com.project.dto.CartResponse;
import com.project.entity.Cart;
import com.project.entity.CartItem;
import com.project.entity.Product;
import com.project.entity.User;
import com.project.exception.BadRequestException;
import com.project.exception.ResourceNotFoundException;
import com.project.repository.CartItemRepository;
import com.project.repository.CartRepository;
import com.project.repository.ProductRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    public CartResponse addToCart(
            String email,
            Long productId,
            Integer quantity) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found"
                        )
                );

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException(
                    "Quantity must be greater than 0"
            );
        }

        if (product.getStock() < quantity) {
            throw new BadRequestException(
                    "Not enough stock available"
            );
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

            int newQuantity =
                    cartItem.getQuantity() + quantity;

            if (newQuantity > product.getStock()) {
                throw new BadRequestException(
                        "Requested quantity exceeds available stock"
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {

            cartItem = new CartItem();

            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);

            cart.getItems().add(cartItem);
        }

        cartItemRepository.save(cartItem);

        return convertToCartResponse(cart);
    }

    public CartResponse getCart(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart not found"
                        )
                );

        return convertToCartResponse(cart);
    }


    public CartResponse updateCartItem(
            Long cartItemId,
            Integer quantity) {

        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found"
                        )
                );

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException(
                    "Quantity must be greater than 0"
            );
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if(!cartItem.getCart().getUser().getEmail().equals(email)){
            throw new BadRequestException("you are not allowed to modify this cart item");
        }

        Product product = cartItem.getProduct();

        if (quantity > product.getStock()) {
            throw new BadRequestException(
                    "Requested quantity exceeds available stock"
            );
        }

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return convertToCartResponse(
                cartItem.getCart()
        );
    }


    public void removeCartItem(Long cartItemId) {

        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Cart item not found"
                        )
                );

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (!cartItem.getCart().getUser().getEmail().equals(email)) {
            throw new BadRequestException(
                    "You are not allowed to remove this cart item"
            );
        }

        cartItemRepository.delete(cartItem);
    }

    private CartResponse convertToCartResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::convertToCartItemResponse)
                .toList();

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                items
        );
    }

    private CartItemResponse convertToCartItemResponse(
            CartItem cartItem) {

        return new CartItemResponse(
                cartItem.getId(),
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity()
        );
    }
}