package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
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
@Table(name = "restaurant_application")
public class RestaurantApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "cuisine_type", nullable = false)
    private String cuisineType;

    @Column(name = "years_in_business")
    private Integer yearsInBusiness;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "application_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    @Column(name = "review_note")
    private String reviewNote;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "applied_at")
    private LocalDate appliedAt;

    @Column(name = "reviewed_at")
    private LocalDate reviewedAt;

    @Column(name = "approved_at")
    @Builder.Default
    private LocalDate approvedAt = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private SuperAdmin reviewedBy;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Restaurant restaurant;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RestaurantUsers restaurantAdmin;

    @PrePersist
    protected void onCreate() {
        LocalDate now = LocalDate.now();
        if (appliedAt == null) {
            appliedAt = now;
        }
        if (applicationStatus == null) {
            applicationStatus = ApplicationStatus.PENDING;
        }
        if (reviewedAt == null) {
            reviewedAt = null;
        }
        if (approvedAt == null) {
            approvedAt = null;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (applicationStatus != null) {
            if (applicationStatus == ApplicationStatus.APPROVED && approvedAt == null) {
                approvedAt = LocalDate.now();
            } else if (applicationStatus == ApplicationStatus.REJECTED && reviewedAt == null) {
                reviewedAt = LocalDate.now();
            }
        }
    }

}
