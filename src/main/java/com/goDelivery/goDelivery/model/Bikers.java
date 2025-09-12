package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.VehicleType;
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
@Table(name = "bikers")
public class Bikers  {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "biker_id", nullable = false)
    private Long bikerId;

    @Column(name = "full_names", nullable = false)
    private String fullNames;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "national_id", nullable = false)
    private String nationalId;

    @Column(name = "license_number", nullable = false)
    private String licenseNumber;

    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "vehicle_plate", nullable = false)
    private String vehiclePlate;

    @Column(name = "vehicle_model", nullable = false)
    private String vehicleModel;

    @Column(name = "profile_image", nullable = false)
    private String profileImage;

    @Column(name = "rating", nullable = false)
    private Float rating;

    @Column(name = "total_deliveries", nullable = false)
    private Integer totalDeliveries;

    @Column(name = "successful_deliveries", nullable = false)
    private Integer successfulDeliveries;

    @Column(name = "current_latitude", nullable = false)
    private Float currentLatitude;

    @Column(name = "current_longitude", nullable = false)
    private Float currentLongitude;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @Column(name = "is_online", nullable = false)
    private boolean isOnline;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "joined_at", nullable = false)
    private LocalDate joinedAt;

    @Column(name = "last_active", nullable = false)
    private LocalDate lastActive;

    // One Biker can deliver many Orders
    @OneToMany(mappedBy = "bikers", fetch = FetchType.LAZY)
    private List<Order> orders;

    // One Biker can receive many Reviews
    @OneToMany(mappedBy = "bikers", fetch = FetchType.LAZY)
    private List<Review> reviews;

    // One Biker updates many Delivery Tracking records
    @OneToMany(mappedBy = "bikers", fetch = FetchType.LAZY)
    private List<DeliveryTracking> trackingUpdates;

    // One Biker earns from many orders
    @OneToMany(mappedBy = "bikers", fetch = FetchType.LAZY)
    private List<BikerEarnings> earnings;


}
