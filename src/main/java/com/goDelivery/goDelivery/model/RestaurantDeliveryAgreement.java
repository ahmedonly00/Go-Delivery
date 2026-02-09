package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_delivery_agreements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDeliveryAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name = "agreement_id", nullable = false)
    private SystemDeliveryAgreement agreement;

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    @Column(name = "accepted_by", nullable = false)
    private String acceptedBy;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
