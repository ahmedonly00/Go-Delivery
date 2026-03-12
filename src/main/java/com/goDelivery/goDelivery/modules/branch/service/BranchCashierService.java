package com.goDelivery.goDelivery.modules.branch.service;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.dtos.order.OrderResponse;
import com.goDelivery.goDelivery.dtos.order.OrderStatusUpdate;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.exception.UnauthorizedException;
import com.goDelivery.goDelivery.mapper.OrderMapper;
import com.goDelivery.goDelivery.model.Bikers;
import com.goDelivery.goDelivery.model.BranchUsers;
import com.goDelivery.goDelivery.model.Branches;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.repository.BikersRepository;
import com.goDelivery.goDelivery.repository.BranchUsersRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchCashierService {

    private final OrderRepository orderRepository;
    private final BikersRepository bikersRepository;
    private final BranchUsersRepository branchUsersRepository;
    private final OrderMapper orderMapper;
    private final OrderStatusUpdateService statusUpdateService;

    private Branches getAuthenticatedCashierBranch() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        BranchUsers branchUser = branchUsersRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Branch cashier not authenticated"));
        return branchUser.getBranch();
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getPendingOrders(Pageable pageable) {
        Long branchId = getAuthenticatedCashierBranch().getBranchId();
        log.info("Fetching pending orders for branch ID: {}", branchId);
        return orderRepository.findByBranch_BranchIdAndOrderStatus(branchId, OrderStatus.PLACED, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Long branchId = getAuthenticatedCashierBranch().getBranchId();
        log.info("Fetching all orders for branch ID: {}", branchId);
        return orderRepository.findAllByBranch_BranchId(branchId, pageable)
                .map(orderMapper::toOrderResponse);
    }

    @Transactional
    public OrderResponse acceptOrder(Long orderId, Integer estimatedPrepTimeMinutes) {
        log.info("Accepting order ID: {} with estimated prep time: {} minutes", orderId, estimatedPrepTimeMinutes);
        Order order = getOrderForAuthenticatedBranch(orderId);

        if (order.getOrderStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Only PLACED orders can be accepted");
        }

        order.setAcceptedAt(LocalDate.now());
        order.setEstimatedPrepTimeMinutes(estimatedPrepTimeMinutes);
        return statusUpdateService.updateOrderStatusWithNotification(order, OrderStatus.CONFIRMED);
    }

    @Transactional
    public OrderResponse markOrderReadyForPickup(Long orderId) {
        log.info("Marking order ID: {} as ready for pickup", orderId);
        Order order = getOrderForAuthenticatedBranch(orderId);

        if (order.getOrderStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Only orders in PREPARING status can be marked as ready for pickup");
        }

        return statusUpdateService.updateOrderStatusWithNotification(order, OrderStatus.READY);
    }

    @Transactional
    public OrderResponse confirmOrderDispatch(Long orderId) {
        log.info("Confirming dispatch for order ID: {}", orderId);
        Order order = getOrderForAuthenticatedBranch(orderId);

        if (order.getOrderStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Only orders in READY status can be dispatched");
        }

        if (order.getBikers() == null) {
            throw new IllegalStateException("Cannot dispatch order: No biker assigned");
        }

        return statusUpdateService.updateOrderStatusWithNotification(order, OrderStatus.PICKED_UP);
    }

    @Transactional
    public OrderResponse updateOrderStatus(OrderStatusUpdate statusUpdate) {
        log.info("Updating status for order ID: {} to {}", statusUpdate.getOrderId(), statusUpdate.getStatus());
        Order order = getOrderForAuthenticatedBranch(statusUpdate.getOrderId());

        OrderStatus newStatus = statusUpdate.getStatus();
        order.setOrderStatus(newStatus);

        LocalDate now = LocalDate.now();
        switch (newStatus) {
            case CONFIRMED -> order.setOrderConfirmedAt(now);
            case PREPARING -> order.setOrderPreparedAt(now);
            case PICKED_UP -> order.setPickedUpAt(now);
            case DELIVERED -> order.setDeliveredAt(now);
            case CANCELLED -> order.setCancelledAt(now);
            default -> { }
        }

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long orderId) {
        log.info("Fetching details for order ID: {}", orderId);
        return orderMapper.toOrderResponse(getOrderForAuthenticatedBranch(orderId));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderTimeline(Long orderId) {
        log.info("Fetching timeline for order ID: {}", orderId);
        return orderMapper.toOrderResponse(getOrderForAuthenticatedBranch(orderId));
    }

    @Transactional
    public OrderResponse assignToDelivery(Long orderId, Long bikerId) {
        log.info("Assigning order ID: {} to biker ID: {}", orderId, bikerId);
        Order order = getOrderForAuthenticatedBranch(orderId);

        Bikers biker = bikersRepository.findByBikerId(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery person not found with id: " + bikerId));

        order.setBikers(biker);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setOrderConfirmedAt(LocalDate.now());

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    private Order getOrderForAuthenticatedBranch(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        Long branchId = getAuthenticatedCashierBranch().getBranchId();
        if (order.getBranch() == null || !order.getBranch().getBranchId().equals(branchId)) {
            throw new UnauthorizedException("Order does not belong to your branch");
        }

        return order;
    }
}
