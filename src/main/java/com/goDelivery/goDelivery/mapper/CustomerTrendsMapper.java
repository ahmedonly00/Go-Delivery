package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.analytics.CustomerTrendsDTO;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomerTrendsMapper {


    public List<CustomerTrendsDTO> toCustomerTrendsDTOs(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return Collections.emptyList();
        }

        // Group orders by customer
        Map<Customer, List<Order>> ordersByCustomer = orders.stream()
                .filter(order -> order.getCustomer() != null)
                .collect(Collectors.groupingBy(Order::getCustomer));

        return ordersByCustomer.entrySet().stream()
                .map(entry -> toCustomerTrendsDTO(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public CustomerTrendsDTO toCustomerTrendsDTO(Customer customer, List<Order> customerOrders) {
        if (customer == null || customerOrders == null || customerOrders.isEmpty()) {
            return null;
        }

        // Calculate metrics
        int orderCount = customerOrders.size();
        BigDecimal totalSpent = customerOrders.stream()
                .map(Order::getFinalAmount)
                .filter(Objects::nonNull)
                .map(amount -> new BigDecimal(amount.toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal averageOrderValue = orderCount > 0 
                ? totalSpent.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Get most recent order
        Optional<Order> mostRecentOrder = customerOrders.stream()
                .max(Comparator.comparing(Order::getOrderPlacedAt));

        // Get popular items
        Map<String, Long> popularItems = customerOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getMenuItem() == null ? "Unknown" : item.getMenuItem().getMenuItemName(),
                    Collectors.summingLong(OrderItem::getQuantity)
                ));

        // Find the most popular item
        Map.Entry<String, Long> mostPopularItem = popularItems.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        // Create a list of PopularItem (currently only including the most popular one)
        List<CustomerTrendsDTO.PopularItem> favoriteItems = new ArrayList<>();
        if (mostPopularItem != null) {
            // For now, we're just using the item name and count
            // You might want to include the menuItemId if available
            CustomerTrendsDTO.PopularItem popularItem = new CustomerTrendsDTO.PopularItem(
                    null, // menuItemId - set to null or get from your data if available
                    mostPopularItem.getKey(),
                    mostPopularItem.getValue().intValue()
            );
            favoriteItems.add(popularItem);
        }

        return CustomerTrendsDTO.builder()
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .customerEmail(customer.getEmail())
                .orderCount(orderCount)
                .totalSpent(totalSpent)
                .averageOrderValue(averageOrderValue)
                .lastOrderDate(mostRecentOrder.map(order -> order.getOrderPlacedAt().atStartOfDay()).orElse(null))
                .favoriteItems(favoriteItems)
                .build();
    }
}
