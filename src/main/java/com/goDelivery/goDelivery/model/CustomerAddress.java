package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.AddressType;
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
@Table(name = "customer_address")
public class CustomerAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "address_id", nullable = false)
    private Long customerAddressId;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "area_name", nullable = false)
    private String areaName;

    @Column(name = "house_number", nullable = false)
    private String houseNumber;

    @Column(name = "local_contact_number", nullable = false)
    private String localContactNumber;

    @Column(name = "latitude", nullable = true)
    private Float latitude;

    @Column(name = "longitude", nullable = true)
    private Float longitude;

    @Column(name = "address_type", nullable = true)
    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    @Column(name = "usage_option", nullable = false)
    private String usageOption;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @PrePersist
    protected void onCreate() {
        LocalDate now = LocalDate.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        LocalDate now = LocalDate.now();
        updatedAt = now;
    }
}
