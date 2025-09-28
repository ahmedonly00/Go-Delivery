package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import com.goDelivery.goDelivery.dtos.order.OrderItemResponse;
import com.goDelivery.goDelivery.dtos.order.OrderRequest;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.OrderItem;
import com.goDelivery.goDelivery.model.Restaurant;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    // convert order to order response
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
                .paymentMethod(order.getPaymentMethod())
                .specialInstructions(order.getSpecialInstructions())
                .estimatedDeliveryTime(order.getEstimatedDeliveryTime())
                .createdAt (LocalDate.now())
                .orderPlacedAt(LocalDate.now())
                .orderConfirmedAt(LocalDate.now())
                .foodReadyAt(LocalDate.now())
                .pickedUpAt(LocalDate.now())
                .deliveredAt(LocalDate.now())
                .cancelledAt(LocalDate.now())
                .cancellationReason(order.getCancellationReason())
                .customerId(order.getCustomer() != null ? order.getCustomer().getCustomerId() : null)
                .restaurantId(order.getRestaurant() != null ? order.getRestaurant().getRestaurantId() : null)
                .bikerId(order.getBikers() != null ? order.getBikers().getBikerId() : null)
                .build();
    }

    // convert order item list to order item response list
    public List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    // convert order item to order item response
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
                .createdAt(LocalDate.now())
                .menuItemId(menuItem != null ? menuItem.getMenuItemId() : null)
                .menuItemName(menuItem != null ? menuItem.getMenuItemName() : null)
                .build();
    }

    // convert order list to order response list
    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    // convert order request to order
    public Order toOrder(OrderRequest orderRequest, Customer customer, Restaurant restaurant) {
        if (orderRequest == null) {
            return null;
        }

        return Order.builder()
            .customer(customer)
            .restaurant(restaurant)
            .orderStatus(OrderStatus.PLACED)
            .paymentStatus(PaymentStatus.PENDING)
            .deliveryAddress(orderRequest.getDeliveryAddress())
            .specialInstructions(orderRequest.getSpecialInstructions())
            .paymentMethod(orderRequest.getPaymentMethod() != null ? orderRequest.getPaymentMethod() : PaymentMenthod.CASH)
            .estimatedDeliveryTime(orderRequest.getEstimatedDelivery())
            .subTotal(orderRequest.getSubTotal())
            .discountAmount(orderRequest.getDiscountAmount() != null ? orderRequest.getDiscountAmount() : 0.0f)
            .deliveryFee(orderRequest.getDeliveryFee() != null ? orderRequest.getDeliveryFee() : 0.0f)
            .finalAmount(orderRequest.getFinalAmount())
            .orderPlacedAt(LocalDate.now())
            .build();
    }

}
