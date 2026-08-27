package com.project.controller;

import com.project.dto.CartResponse;
import com.project.security.SecurityUtility;
import com.project.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        String email = SecurityUtility.getCurrentUserEmail();

        return ResponseEntity.ok(
                cartService.addToCart(
                        email,
                        productId,
                        quantity
                )
        );
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @PathVariable Long userId) {
        String email = SecurityUtility.getCurrentUserEmail();

        return ResponseEntity.ok(
                cartService.getCart(email)
        );
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        cartItemId,
                        quantity
                )
        );
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long cartItemId) {

        cartService.removeCartItem(cartItemId);

        return ResponseEntity.noContent().build();
    }
}