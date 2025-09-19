package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.order.OrderItemResponse;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .subTotal(order.getSubTotal())
                .deliveryFee(order.getDeliveryFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .specialInstructions(order.getSpecialInstructions())
                .estimatedDelivery(order.getEstimatedDelivery())
                .orderPlacedAt(order.getOrderPlacedAt())
                .orderConfirmedAt(order.getOrderConfirmedAt())
                .foodReadyAt(order.getFoodReadyAt())
                .pickedUpAt(order.getPickedUpAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .customerId(order.getCustomer() != null ? order.getCustomer().getCustomerId() : null)
                .restaurantId(order.getRestaurant() != null ? order.getRestaurant().getRestaurantId() : null)
                .branchId(order.getBranch() != null ? order.getBranch().getBranchId() : null)
                .bikerId(order.getBikers() != null ? order.getBikers().getBikerId() : null)
                .deliveryAddressId(order.getDeliveryAddress() != null ? order.getDeliveryAddress().getAddressId() : null)
                .build();
    }

    public List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        MenuItem menuItem = orderItem.getMenuItem();
        
        return OrderItemResponse.builder()
                .orderItemId(orderItem.getOrderItemId())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .specialRequests(orderItem.getSpecialRequests())
                .createdAt(orderItem.getCreatedAt())
                .menuItemId(menuItem != null ? menuItem.getMenuItemId() : null)
                .menuItemName(menuItem != null ? menuItem.getItemName() : null)
                .orderId(orderItem.getOrder() != null ? orderItem.getOrder().getOrderId() : null)
                .build();
    }

    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }
}
