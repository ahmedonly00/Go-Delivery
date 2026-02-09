package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_delivery_agreements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemDeliveryAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String version;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String agreementText;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Column(name = "commission_percentage")
    private Float commissionPercentage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
