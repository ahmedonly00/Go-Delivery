package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.ApprovalStatus;
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
    
    @Column(name = "website")
    private String website;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;
    
    @Column(name = "business_document_url")
    private String businessDocumentUrl;
    
    @Column(name = "operating_license_url")
    private String operatingLicenseUrl;
    
    @Column(name = "description", length = 1000)
    private String description;
    
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
    
    // Delivery settings
    @Column(name = "delivery_available")
    @Builder.Default
    private Boolean deliveryAvailable = false;
    
    @Column(name = "delivery_radius")
    private Float deliveryRadius;
    
    @Column(name = "minimum_order_amount")
    private Float minimumOrderAmount;
    
    @Column(name = "delivery_fee")
    private Float deliveryFee;
    
    // Ratings
    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;
    
    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

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

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private List<Order> orders;

}
