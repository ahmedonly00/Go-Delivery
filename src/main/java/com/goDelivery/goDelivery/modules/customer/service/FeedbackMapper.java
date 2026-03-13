package com.goDelivery.goDelivery.modules.customer.service;

import com.goDelivery.goDelivery.modules.customer.dto.FeedbackRequest;
import com.goDelivery.goDelivery.modules.customer.dto.FeedbackResponse;
import com.goDelivery.goDelivery.modules.customer.model.Customer;
import com.goDelivery.goDelivery.modules.customer.model.Feedback;
import com.goDelivery.goDelivery.modules.ordering.model.Order;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public Feedback toEntity(FeedbackRequest request, Order order, Customer customer) {
        Feedback feedback = new Feedback();
        feedback.setOrder(order);
        feedback.setCustomer(customer);
        feedback.setFoodRating(request.getFoodRating());
        feedback.setDeliveryRating(request.getDeliveryRating());
        feedback.setOverallRating(request.getOverallRating());
        feedback.setFoodReview(request.getFoodReview());
        feedback.setDeliveryReview(request.getDeliveryReview());
        return feedback;
    }

    public FeedbackResponse toDto(Feedback feedback) {
        FeedbackResponse response = new FeedbackResponse();
        response.setId(feedback.getId());
        response.setOrderId(feedback.getOrder().getOrderId());
        response.setCustomerId(feedback.getCustomer().getCustomerId());
        response.setCustomerName(feedback.getCustomer().getFullNames());
        response.setFoodRating(feedback.getFoodRating());
        response.setDeliveryRating(feedback.getDeliveryRating());
        response.setOverallRating(feedback.getOverallRating());
        response.setFoodReview(feedback.getFoodReview());
        response.setDeliveryReview(feedback.getDeliveryReview());
        response.setCreatedAt(feedback.getCreatedAt());
        return response;
    }
}
