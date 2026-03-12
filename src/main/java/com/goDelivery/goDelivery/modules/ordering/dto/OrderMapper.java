package com.goDelivery.goDelivery.modules.ordering.mapper;

import com.goDelivery.goDelivery.shared.enums.OrderStatus;
import com.goDelivery.goDelivery.shared.enums.PaymentMenthod;
import com.goDelivery.goDelivery.shared.enums.PaymentStatus;
import com.goDelivery.goDelivery.modules.ordering.dto.OrderItemResponse;
import com.goDelivery.goDelivery.modules.ordering.dto.OrderRequest;
import com.goDelivery.goDelivery.modules.ordering.dto.OrderResponse;
import com.goDelivery.goDelivery.modules.branch.model.Branches;
import com.goDelivery.goDelivery.modules.branch.model.BranchMenuItem;
import com.goDelivery.goDelivery.modules.customer.model.Customer;
import com.goDelivery.goDelivery.modules.restaurant.model.MenuItem;
import com.goDelivery.goDelivery.modules.ordering.model.Order;
import com.goDelivery.goDelivery.modules.ordering.model.OrderItem;
import com.goDelivery.goDelivery.modules.restaurant.model.Restaurant;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .deliveryAddress(order.getDeliveryAddress())
                .phoneNumber(order.getCustomer().getPhoneNumber())
                .paymentStatus(order.getPaymentStatus())
                .subTotal(order.getSubTotal())
                .deliveryFee(order.getDeliveryFee())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod())
                .specialInstructions(order.getSpecialInstructions())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderPlacedAt(order.getOrderPlacedAt())
                .orderConfirmedAt(order.getOrderConfirmedAt())
                .OrderPreparedAt(order.getOrderPreparedAt())
                .pickedUpAt(order.getPickedUpAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .customerId(order.getCustomer() != null ? order.getCustomer().getCustomerId() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullNames() : null)
                .restaurantId(order.getRestaurant() != null ? order.getRestaurant().getRestaurantId() : null)
                .restaurantName(order.getRestaurant() != null ? order.getRestaurant().getRestaurantName() : null)
                .branchId(order.getBranch() != null ? order.getBranch().getBranchId() : null)
                .branchName(order.getBranch() != null ? order.getBranch().getBranchName() : null)
                .bikerId(order.getBikers() != null ? order.getBikers().getBikerId() : null)
                .items(toOrderItemResponseList(order.getOrderItems())) // Add this line to include order items
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
        BranchMenuItem branchMenuItem = orderItem.getBranchMenuItem();

        Long menuItemId = menuItem != null ? menuItem.getMenuItemId()
                : (branchMenuItem != null ? branchMenuItem.getMenuItemId() : null);
        String menuItemName = menuItem != null ? menuItem.getMenuItemName()
                : (branchMenuItem != null ? branchMenuItem.getMenuItemName() : null);

        return OrderItemResponse.builder()
                .orderItemId(orderItem.getOrderItemId())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .totalPrice(orderItem.getTotalPrice())
                .specialRequests(orderItem.getSpecialRequests())
                .createdAt(LocalDate.now())
                .menuItemId(menuItemId)
                .menuItemName(menuItemName)
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
    public Order toOrder(OrderRequest orderRequest, Customer customer, Restaurant restaurant, Branches branch) {
        if (orderRequest == null) {
            return null;
        }

        return Order.builder()
                .customer(customer)
                .restaurant(restaurant)
                .branch(branch)
                .orderStatus(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .deliveryAddress(orderRequest.getDeliveryAddress())
                .specialInstructions(orderRequest.getSpecialInstructions())
                .paymentMethod(
                        orderRequest.getPaymentMethod() != null ? orderRequest.getPaymentMethod() : PaymentMenthod.CASH)
                .subTotal(orderRequest.getSubTotal())
                .discountAmount(orderRequest.getDiscountAmount() != null ? orderRequest.getDiscountAmount() : 0.0f)
                .deliveryFee(orderRequest.getDeliveryFee() != null ? orderRequest.getDeliveryFee() : 0.0f)
                .finalAmount(orderRequest.getFinalAmount())
                .orderPlacedAt(LocalDateTime.now())
                .build();
    }

}
