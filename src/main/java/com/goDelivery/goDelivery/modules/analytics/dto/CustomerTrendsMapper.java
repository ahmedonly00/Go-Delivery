package com.goDelivery.goDelivery.modules.analytics.dto;

import com.goDelivery.goDelivery.modules.customer.model.Customer;
import com.goDelivery.goDelivery.modules.ordering.model.Order;
import com.goDelivery.goDelivery.modules.ordering.model.OrderItem;
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

                int orderCount = customerOrders.size();
                BigDecimal totalSpent = customerOrders.stream()
                                .map(Order::getFinalAmount)
                                .filter(Objects::nonNull)
                                .map(amount -> new BigDecimal(amount.toString()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal averageOrderValue = orderCount > 0
                                ? totalSpent.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                Optional<Order> mostRecentOrder = customerOrders.stream()
                                .max(Comparator.comparing(Order::getOrderPlacedAt));

                Map<String, Long> popularItems = customerOrders.stream()
                                .flatMap(order -> order.getOrderItems().stream())
                                .collect(Collectors.groupingBy(
                                                item -> item.getMenuItem() == null ? "Unknown"
                                                                : item.getMenuItem().getMenuItemName(),
                                                Collectors.summingLong(OrderItem::getQuantity)));

                Map.Entry<String, Long> mostPopularItem = popularItems.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .orElse(null);

                List<CustomerTrendsDTO.PopularItem> favoriteItems = new ArrayList<>();
                if (mostPopularItem != null) {
                        favoriteItems.add(new CustomerTrendsDTO.PopularItem(
                                        null,
                                        mostPopularItem.getKey(),
                                        mostPopularItem.getValue().intValue()));
                }

                return CustomerTrendsDTO.builder()
                                .customerId(customer.getCustomerId())
                                .customerName(customer.getFullNames())
                                .customerEmail(customer.getEmail())
                                .orderCount(orderCount)
                                .totalSpent(totalSpent)
                                .averageOrderValue(averageOrderValue)
                                .lastOrderDate(mostRecentOrder.map(Order::getOrderPlacedAt).orElse(null))
                                .favoriteItems(favoriteItems)
                                .build();
        }
}
