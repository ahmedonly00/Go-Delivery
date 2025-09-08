package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.CustomerStatus;
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
@Table(name = "customer_analytics")
public class CustomerAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "analytics_id", nullable = false)
    private Long analyticsId;

    @Column(name = "total_order", nullable = false)
    private Integer totalOrder;

    @Column(name = "total_spent", nullable = false)
    private Float totalSpent;

    @Column(name = "average_order_value", nullable = false)
    private Float averageOrderValue;

    @Column(name = "favorite_cuisines", nullable = false)
    private String favoriteCuisines;

    @Column(name = "last_order_date", nullable = false)
    private LocalDate lastOrderDate;

    @Column(name = "customer_status", nullable = false)
    private CustomerStatus customerStatus;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    // One Analytics record belongs to One Customer
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;


}
