package com.goDelivery.goDelivery.model;

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
@Table(name = "order_analytics")
public class OrderAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long analyticsId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Column(name = "total_revenue", nullable = false)
    private Float totalRevenue;

    @Column(name = "average_order_value", nullable = false)
    private Float averageOrderValue;

    @Column(name = "peak_hour", nullable = false)
    private Integer peakHour;

    @Column(name = "popular_items", nullable = false)
    private String popularItems;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;


    // Many Analytics records belong to One Restaurant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
}
