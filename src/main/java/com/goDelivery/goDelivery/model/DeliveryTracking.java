package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "delivery_tracking")
public class DeliveryTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "tracking_id", nullable = false)
    private Long trackingId;

    @Column(name = "delivery_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "notes")
    private String notes;

    @Column(name = "time_stamp", nullable = false)
    private Timestamp timestamp;

    // New fields for live tracking
    @Column(name = "estimated_arrival_time")
    private LocalDateTime estimatedArrivalTime;

    @Column(name = "current_status_message")
    private String currentStatusMessage;

    @Column(name = "distance_to_destination_km")
    private Double distanceToDestinationKm;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Many Tracking records belong to One Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Many Tracking records updated by One Biker
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biker_id", nullable = false)
    private Bikers bikers;

}
