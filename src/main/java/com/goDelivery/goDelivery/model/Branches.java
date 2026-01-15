package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.ApprovalStatus;
import com.goDelivery.goDelivery.Enum.BranchSetupStatus;
import com.goDelivery.goDelivery.Enum.DeliveryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branches")
public class Branches {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "latitude")
    private Float latitude;

    @Column(name = "longitude")
    private Float longitude;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    
    @Column(name = "email")
    private String email;

    // Setup status tracking
    @Enumerated(EnumType.STRING)
    @Column(name = "setup_status")
    @Builder.Default
    private BranchSetupStatus setupStatus = BranchSetupStatus.ACCOUNT_CREATED;
    
    // Delivery configuration
    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false)
    private DeliveryType deliveryType ;
    
    @Column(name = "delivery_fee")
    private Float deliveryFee;
    
    @Column(name = "delivery_radius")
    private Double deliveryRadius;
    
    @Column(name = "minimum_order_amount")
    private Float minimumOrderAmount;
    
    @Column(name = "average_preparation_time")
    private Integer averagePreparationTime;
    
    @Column(name = "delivery_available")
    private Boolean deliveryAvailable;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private ApprovalStatus approvalStatus;
    
    // Business documents
    @Column(name = "business_document_url")
    private String businessDocumentUrl;
    
    @Column(name = "operating_license_url")
    private String operatingLicenseUrl;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "cuisine_type")
    private String cuisineType;
    
    @Column(name = "tax_identification_number")
    private String taxIdentificationNumber;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    @Column(name = "approved_at")
    private LocalDate approvedAt;
    
    @Column(name = "reviewed_by")
    private String reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDate reviewedAt;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    // Ratings
    @Column(name = "average_rating")
    private Double averageRating;
    
    @Column(name = "review_count")
    private Integer reviewCount;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
    
    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BranchUsers> branchUsers = new ArrayList<>();
    
    @OneToOne(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OperatingHours operatingHours;

}