package com.goDelivery.goDelivery.controller;

import com.goDelivery.goDelivery.dtos.cart.CartItemDTO;
import com.goDelivery.goDelivery.dtos.cart.ShoppingCartDTO;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.repository.CustomerRepository;
import com.goDelivery.goDelivery.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    private final CartService cartService;
    private final CustomerRepository customerRepository;

    @GetMapping(value = "/getCart")
    public ResponseEntity<ShoppingCartDTO> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
                Customer customer = customerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Customer not found"));       
        return ResponseEntity.ok(cartService.getCart(customer.getCustomerId()));
    }

    @PostMapping(value = "/addItem")
    public ResponseEntity<ShoppingCartDTO> addItemToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CartItemDTO cartItemDTO) {
                Customer customer = customerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Customer not found"));       
        return ResponseEntity.ok(cartService.addItemToCart(customer.getCustomerId(), cartItemDTO));
    }

    @PutMapping(value = "/updateItem/{itemId}")
    public ResponseEntity<ShoppingCartDTO> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestBody CartItemDTO cartItemDTO) {
                Customer customer = customerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Customer not found"));       
        return ResponseEntity.ok(cartService.updateCartItem(customer.getCustomerId(), itemId, cartItemDTO));
    }

    @DeleteMapping(value = "/removeItem/{itemId}")
    public ResponseEntity<ShoppingCartDTO> removeItemFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
                Customer customer = customerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Customer not found"));       
        return ResponseEntity.ok(cartService.removeItemFromCart(customer.getCustomerId(), itemId));
    }

    @DeleteMapping(value = "/clearCart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
                Customer customer = customerRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Customer not found"));       
        cartService.clearCart(customer.getCustomerId());
        return ResponseEntity.noContent().build();
    }
}
