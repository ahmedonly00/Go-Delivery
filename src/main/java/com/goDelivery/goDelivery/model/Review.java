package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(name = "food_rating", nullable = false)
    private Integer foodRating;

    @Column(name = "delivery_rating", nullable = false)
    private Integer deliveryRating;

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    @Column(name = "review_text", nullable = false)
    private String reviewText;

    @Column(name = "review_images", nullable = false)
    private String reviewImages;

    @Column(name = "is_anonymous", nullable = false)
    private boolean isAnonymous;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified;

    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // One Review belongs to One Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Many Reviews written by One Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Many Reviews received by One Restaurant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Many Reviews about One Biker (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biker_id")
    private Bikers bikers;

    // One Review can get many Responses
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewResponse> responses;


}
