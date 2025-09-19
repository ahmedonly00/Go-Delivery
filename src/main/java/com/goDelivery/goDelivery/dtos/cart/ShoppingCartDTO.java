package com.goDelivery.goDelivery.dtos.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartDTO {
    private Long id;
    private Long customerId;
    private List<CartItemDTO> items;
    private Double totalPrice;
    private String createdAt;
    private String updatedAt;
}
