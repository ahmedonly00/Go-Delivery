package com.goDelivery.goDelivery.modules.customer.service;

import com.goDelivery.goDelivery.shared.enums.OrderStatus;
import com.goDelivery.goDelivery.modules.customer.dto.FeedbackRequest;
import com.goDelivery.goDelivery.modules.customer.dto.FeedbackResponse;
import com.goDelivery.goDelivery.shared.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.modules.customer.model.Customer;
import com.goDelivery.goDelivery.modules.customer.model.Feedback;
import com.goDelivery.goDelivery.modules.ordering.model.Order;
import com.goDelivery.goDelivery.modules.customer.repository.CustomerRepository;
import com.goDelivery.goDelivery.modules.customer.repository.FeedbackRepository;
import com.goDelivery.goDelivery.modules.ordering.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final FeedbackMapper feedbackMapper;

    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest request, Long customerId) {
        Order order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        if (!order.getOrderStatus().equals(OrderStatus.DELIVERED)) {
            throw new IllegalStateException("Feedback can only be submitted for delivered orders");
        }

        if (feedbackRepository.existsByOrder(order)) {
            throw new IllegalStateException("Feedback already submitted for this order");
        }

        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (!order.getCustomer().equals(customer)) {
            throw new SecurityException("You can only submit feedback for your own orders");
        }

        Feedback feedback = feedbackMapper.toEntity(request, order, customer);
        Feedback savedFeedback = feedbackRepository.save(feedback);

        return feedbackMapper.toDto(savedFeedback);
    }

    public List<FeedbackResponse> getFeedbackByCustomer(Long customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return feedbackRepository.findByCustomer(customer).stream()
                .map(feedbackMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<FeedbackResponse> getFeedbackByOrder(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        return feedbackRepository.findByOrder(order).stream()
                .map(feedbackMapper::toDto)
                .collect(Collectors.toList());
    }
}
