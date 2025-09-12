package com.goDelivery.goDelivery.dtos.menu;

import java.util.List;

import com.goDelivery.goDelivery.model.MenuCategory;
import com.goDelivery.goDelivery.model.MenuItemVariant;
import com.goDelivery.goDelivery.model.OrderItem;
import com.goDelivery.goDelivery.model.Restaurant;

import lombok.Data;

@Data
public class MenuItemDTO {
    private Long itemId;

    private String itemName;

    private String description;

    private Float price;

    private String image;

    private String ingredients;

    private boolean isAvailable;

    private Integer preparationTime;

    private Integer preparationScore;

    private Restaurant restaurant;

    private List<MenuItemVariant> variants;

    private MenuCategory category;

    private List<OrderItem> orderItems;
}
