package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
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
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "sub_total", nullable = false)
    private Float subTotal;

    @Column(name = "delivery_fee", nullable = false)
    private Float deliveryFee;

    @Column(name = "discount_amount", nullable = false)
    private Float discountAmount;

    @Column(name = "final_amount", nullable = false)
    private Float finalAmount;

    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMenthod paymentMethod;

    @Column(name = "special_instructions", nullable = false)
    private String specialInstructions;

    @Column(name = "estimated_delivery_time", nullable = false)
    private LocalDate estimatedDeliveryTime;

    @Column(name = "order_placed_at", nullable = false)
    private LocalDate orderPlacedAt;

    @Column(name = "order_confirmed_at", nullable = false)
    private LocalDate orderConfirmedAt;

    @Column(name = "food_ready_at", nullable = false)
    private LocalDate foodReadyAt;

    @Column(name = "picked_up_at", nullable = false)
    private LocalDate pickedUpAt;

    @Column(name = "delivered_at", nullable = false)
    private LocalDate deliveredAt;

    @Column(name = "cancelled_at", nullable = false)
    private LocalDate cancelledAt;

    @Column(name = "cancellation_reason", nullable = false)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branches branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biker_id")
    private Bikers bikers;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "delivery_address_id", nullable = false)
    // private CustomerAddress deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    // One Order has One Payment
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    // One Order can have One Review
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Review review;

    // One Order can be tracked by many Delivery Tracking records
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeliveryTracking> trackingHistory;

    // One Order affects many Wallet Transactions
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<WalletTransaction> walletTransactions;

}
