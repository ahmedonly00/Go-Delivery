package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "review_response")
public class ReviewResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "response_id", nullable = false)
    private Long responseId;

    @Column(name = "response_text", nullable = false)
    private String responseText;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // Many Responses belong to One Review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    // Many Responses written by One Restaurant User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_user_id", nullable = false)
    private RestaurantUsers restaurantUser;

    @PrePersist
    protected void onCreate() {
        LocalDate now = LocalDate.now();
        createdAt = now;
    }

}
