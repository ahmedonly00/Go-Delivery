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
@Table(name = "biker_earnings")
public class BikerEarnings {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "earning_id", nullable = false)
    private Long earningId;

    @Column(name = "base_fee", nullable = false)
    private Float baseFee;

    @Column(name = "distance_fee", nullable = false)
    private Float distanceFee;

    @Column(name = "tip_amount", nullable = false)
    private Float tipAmount;

    @Column(name = "bonus_amount", nullable = false)
    private Float bonusAmount;

    @Column(name = "total_earning", nullable = false)
    private Float totalEarning;

    @Column(name = "earning_date", nullable = false)
    private LocalDate earningDate;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // Many Earnings belong to One Biker
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biker_id", nullable = false)
    private Bikers biker;

    // Each Earning is from One Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;


}
