package com.goDelivery.goDelivery.mapper;

import com.goDelivery.goDelivery.dtos.feedback.FeedbackRequest;
import com.goDelivery.goDelivery.dtos.feedback.FeedbackResponse;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.Feedback;
import com.goDelivery.goDelivery.model.Order;
import org.springframework.stereotype.Component;

@Component
public class FeedbackMapper {

    public Feedback toEntity(FeedbackRequest request, Order order, Customer customer) {
        if (request == null) {
            return null;
        }

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
        if (feedback == null) {
            return null;
        }

        FeedbackResponse response = new FeedbackResponse();
        response.setId(feedback.getId());
        response.setOrderId(feedback.getOrder().getOrderId());
        response.setCustomerId(feedback.getCustomer().getCustomerId());
        response.setCustomerName(feedback.getCustomer().getFullName());
        response.setFoodRating(feedback.getFoodRating());
        response.setDeliveryRating(feedback.getDeliveryRating());
        response.setOverallRating(feedback.getOverallRating());
        response.setFoodReview(feedback.getFoodReview());
        response.setDeliveryReview(feedback.getDeliveryReview());
        response.setCreatedAt(feedback.getCreatedAt());
        return response;
    }
}
