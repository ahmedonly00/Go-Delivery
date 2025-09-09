package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.PromotionType;
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
@Table(name = "promotion")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "promo_code", nullable = false)
    private String promoCode;

    @Column(name = "promotion_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PromotionType promotionType;

    @Column(name = "discount_percentage", nullable = false)
    private Float discountPercentage;

    @Column(name = "discount_amount", nullable = false)
    private Float discountAmount;

    @Column(name = "minimum_order_amount", nullable = false)
    private Float minimumOrderAmount;

    @Column(name = "maximum_discount_amount", nullable = false)
    private Float maximumDiscountAmount;

    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    private List<Order> orders;


}
