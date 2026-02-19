package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.Enum.DistanceUnit;
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

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "cuisine_type", nullable = false)
    private String cuisineType;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "logo_url")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "setup_status")
    private RestaurantSetupStatus setupStatus;

    @Column(name = "rating")
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    @Builder.Default
    private DeliveryType deliveryType = DeliveryType.SYSTEM_DELIVERY; // Default to system delivery

    @Column(name = "delivery_fee")
    private Float deliveryFee; // Only applicable if deliveryType is SELF_DELIVERY

    @Column(name = "delivery_radius")
    private Double deliveryRadius; // Delivery radius value, unit specified by radiusUnit

    @Enumerated(EnumType.STRING)
    @Column(name = "radius_unit")
    @Builder.Default
    private DistanceUnit radiusUnit = DistanceUnit.KILOMETERS; // Unit for delivery radius

    @Column(name = "base_delivery_fee")
    private Float baseDeliveryFee; // Base fee for SELF_DELIVERY

    @Column(name = "per_km_fee")
    private Float perKmFee; // Additional fee per km for SELF_DELIVERY

    @Column(name = "description")
    private String description;

    @Column(name = "minimum_order_amount")
    private Float minimumOrderAmount;

    // Business Documents
    @Column(name = "commercial_registration_certificate_url")
    private String commercialRegistrationCertificateUrl;

    @Column(name = "tax_identification_number")
    private String taxIdentificationNumber;

    @Column(name = "tax_identification_document_url")
    private String taxIdentificationDocumentUrl;

    // Approval fields
    @Column(name = "is_approved")
    @Builder.Default
    private Boolean isApproved = false;

    @Column(name = "approval_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.goDelivery.goDelivery.Enum.ApprovalStatus approvalStatus = com.goDelivery.goDelivery.Enum.ApprovalStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Transient
    private Double distanceFromUser;

    @Column(name = "reviewed_by")
    private String reviewedBy; // Super admin email who reviewed

    @Column(name = "reviewed_at")
    private LocalDate reviewedAt;

    @Column(name = "average_preparation_time")
    private Integer averagePreparationTime;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuItem> menuItems;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MenuCategory> menuCategories;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Promotion> promotions;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY)
    private List<RestaurantUsers> restaurantUsers = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderAnalytics> orderAnalytics;

    @OneToOne(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OperatingHours operatingHours;

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
