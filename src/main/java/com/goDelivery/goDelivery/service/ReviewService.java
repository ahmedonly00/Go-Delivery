package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.review.CreateReviewRequest;
import com.goDelivery.goDelivery.dtos.review.ReviewResponseDTO;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.ReviewMapper;
import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final BikersRepository bikerRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponseDTO createReview(CreateReviewRequest request) {
        // Verify order exists and is completed
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));
        
        if (order.getOrderStatus() != OrderStatus.DELIVERED && order.getOrderStatus() != OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot review an order that is not completed (status: " + order.getOrderStatus() + ")");
        }
        
        // Check if review already exists for this order
        if (reviewRepository.existsByOrder_OrderId(order.getOrderId())) {
            throw new IllegalStateException("A review already exists for this order");
        }
        
        // Verify customer exists
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));
        
        // Verify restaurant exists
        Restaurant restaurant = restaurantRepository.findByRestaurantId(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + request.getRestaurantId()));
        
        // Verify biker if provided
        Bikers biker = null;
        if (request.getBikerId() != null) {
            biker = bikerRepository.findById(request.getBikerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + request.getBikerId()));
            
            if (request.getDeliveryRating() == null) {
                throw new IllegalArgumentException("Delivery rating is required when reviewing a biker");
            }
        }
        
        // Calculate overall rating (average of food and delivery ratings if both exist)
        Integer overallRating = request.getDeliveryRating() != null ? 
                (request.getFoodRating() + request.getDeliveryRating()) / 2 : 
                request.getFoodRating();
        
        // Create and save the review
        Review review = Review.builder()
                .order(order)
                .customer(customer)
                .restaurant(restaurant)
                .bikers(biker)
                .foodRating(request.getFoodRating())
                .deliveryRating(request.getDeliveryRating())
                .overallRating(overallRating)
                .reviewText(request.getReviewText())
                .reviewImages(request.getReviewImages())
                .isAnonymous(request.isAnonymous())
                .isVerified(false)
                .helpfulCount(0)
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        // Update restaurant's average rating
        updateRestaurantRating(restaurant.getRestaurantId());
        
        // Update biker's average rating if reviewed
        if (biker != null) {
            updateBikerRating(biker.getBikerId());
        }
        
        return reviewMapper.toReviewResponseDTO(savedReview);
    }
    
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getRestaurantReviews(Long restaurantId) {
        return reviewRepository.findByRestaurant_RestaurantId(restaurantId).stream()
                .map(reviewMapper::toReviewResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getBikerReviews(Long bikerId) {
        return reviewRepository.findByBikers_BikerId(bikerId).stream()
                .map(reviewMapper::toReviewResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getCustomerReviews(Long customerId) {
        return reviewRepository.findByCustomer_CustomerId(customerId).stream()
                .map(reviewMapper::toReviewResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void markReviewAsHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }
    
    @Transactional
    public void verifyReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        
        if (review.isVerified()) {
            throw new IllegalStateException("Review is already verified");
        }
        
        review.setVerified(true);
        reviewRepository.save(review);
    }
    
    private void updateRestaurantRating(Long restaurantId) {
        Double averageRating = reviewRepository.findAverageFoodRatingByRestaurantId(restaurantId);
        int reviewCount = reviewRepository.countByRestaurantId(restaurantId);
        
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        
        restaurant.setRating(averageRating);
        restaurant.setTotalReviews(reviewCount);
        restaurantRepository.save(restaurant);
    }
    
    private void updateBikerRating(Long bikerId) {
        Double averageRating = reviewRepository.findAverageDeliveryRatingByBikerId(bikerId);
        
        Bikers biker = bikerRepository.findById(bikerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biker not found with id: " + bikerId));
        
        biker.setRating(averageRating != null ? averageRating.floatValue() : null);
        bikerRepository.save(biker);
    }
}
