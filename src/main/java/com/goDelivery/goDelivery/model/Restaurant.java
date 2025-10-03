package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import com.goDelivery.goDelivery.Enum.RestaurantSetupStatus;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "cuisine_type", nullable = false)
    private String cuisineType;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "rating")
    private Float rating;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "description")
    private String description;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "delivery_radius")
    @Builder.Default
    private Double deliveryRadius = 5.0; // Default 5km

    @Builder.Default
    @Column(name = "is_active")
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "setup_status")
    @Enumerated(EnumType.STRING)
    private RestaurantSetupStatus setupStatus = RestaurantSetupStatus.ACCOUNT_CREATED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private RestaurantUsers createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "average_preparation_time")
    private Integer averagePreparationTime;

    @Column(name = "delivery_fee")
    private Float deliveryFee;

    @Column(name = "minimum_order_amount")
    private Float minimumOrderAmount;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    private OperatingHours operatingHours;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private RestaurantApplication application;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Branches> branches;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantUsers> employees;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuCategory> menuCategories;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Promotion> promotions;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderAnalytics> orderAnalytics;
    

    @PrePersist
    protected void onCreate() {
        LocalDate now = LocalDate.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }

}
