package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "food_rating", nullable = false)
    private Integer foodRating; // 1-5 stars

    @Column(name = "delivery_rating", nullable = false)
    private Integer deliveryRating; // 1-5 stars

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating; // 1-5 stars

    @Column(name = "food_review", columnDefinition = "TEXT")
    private String foodReview;

    @Column(name = "delivery_review", columnDefinition = "TEXT")
    private String deliveryReview;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
