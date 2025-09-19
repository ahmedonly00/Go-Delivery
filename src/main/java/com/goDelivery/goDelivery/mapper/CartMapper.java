package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.cart.CartItemDTO;
import com.goDelivery.goDelivery.dtos.cart.ShoppingCartDTO;
import com.goDelivery.goDelivery.model.CartItem;
import com.goDelivery.goDelivery.model.ShoppingCart;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    //Convert CartItem to CartItemDTO
    public CartItemDTO toCartItemDTO(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        return CartItemDTO.builder()
                .id(cartItem.getId())
                .menuItemId(cartItem.getMenuItem().getItemId())
                .menuItemName(cartItem.getMenuItem().getItemName())
                .price(cartItem.getMenuItem().getPrice().doubleValue())
                .quantity(cartItem.getQuantity())
                .specialInstructions(cartItem.getSpecialInstructions())
                .imageUrl(cartItem.getMenuItem().getImage())
                .build();
    }

    //Convert ShoppingCart to ShoppingCartDTO
    public ShoppingCartDTO toShoppingCartDTO(ShoppingCart cart) {
        if (cart == null) {
            return null;
        }

        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());

        return ShoppingCartDTO.builder()
                .id(cart.getId())
                .customerId(cart.getCustomer().getCustomerId())
                .items(itemDTOs)
                .totalPrice(cart.getTotalPrice())
                .createdAt(cart.getCreatedAt().toString())
                .updatedAt(cart.getUpdatedAt() != null ? cart.getUpdatedAt().toString() : null)
                .build();
    }
}
