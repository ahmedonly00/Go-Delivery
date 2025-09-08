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

    @Column(name = "application_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    @Column(name = "rejection_reason", nullable = false)
    private String rejectionReason;

    @Column(name = "applied_at", nullable = false)
    private LocalDate appliedAt;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDate reviewedAt;

    @Column(name = "approved_at", nullable = false)
    private LocalDate approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private SuperAdmin reviewedBy;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Restaurant restaurant;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RestaurantUsers restaurantAdmin;

}
