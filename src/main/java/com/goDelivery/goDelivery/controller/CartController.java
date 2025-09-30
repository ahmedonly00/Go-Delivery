package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.cart.CartItemDTO;
import com.goDelivery.goDelivery.dtos.cart.ShoppingCartDTO;
import com.goDelivery.goDelivery.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    private final CartService cartService;

    @GetMapping("/getCart")
    public ResponseEntity<ShoppingCartDTO> getCart(@RequestParam Long customerId) {
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/addItem")
    public ResponseEntity<ShoppingCartDTO> addItemToCart(
            @RequestParam Long customerId,
            @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.addItemToCart(customerId, cartItemDTO));
    }

    @PutMapping("/updateItem/{itemId}")
    public ResponseEntity<ShoppingCartDTO> updateCartItem(
            @RequestParam Long customerId,
            @PathVariable Long itemId,
            @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.updateCartItem(customerId, itemId, cartItemDTO));
    }

    @DeleteMapping("/removeItem/{itemId}")
    public ResponseEntity<ShoppingCartDTO> removeItemFromCart(
            @RequestParam Long customerId,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(customerId, itemId));
    }

    @DeleteMapping("/clearCart")
    public ResponseEntity<Void> clearCart(@RequestParam Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
