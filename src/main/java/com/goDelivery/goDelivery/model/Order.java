package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.DisbursementStatus;
import com.goDelivery.goDelivery.Enum.OrderStatus;
import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "order_number")
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

    @Column(name = "accepted_at", nullable = true)
    private LocalDate acceptedAt;

    @Column(name = "estimated_prep_time_minutes", nullable = true)
    private Integer estimatedPrepTimeMinutes;

    @Column(name = "actual_prep_completed_at", nullable = true)
    private LocalDate actualPrepCompletedAt;

    @Column(name = "final_amount", nullable = false)
    private Float finalAmount;

    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMenthod paymentMethod;

    @Column(name = "special_instructions", nullable = true)
    private String specialInstructions;

    @Column(name = "order_placed_at", nullable = false)
    private LocalDate orderPlacedAt;

    @Column(name = "order_confirmed_at", nullable = true)
    private LocalDate orderConfirmedAt;

    @Column(name = "order_prepared_at", nullable = true)
    private LocalDate OrderPreparedAt;

    @Column(name = "picked_up_at", nullable = true)
    private LocalDate pickedUpAt;

    @Column(name = "delivered_at", nullable = true)
    private LocalDate deliveredAt;

    @Column(name = "cancelled_at", nullable = true)
    private LocalDate cancelledAt;

    @Column(name = "cancellation_reason", nullable = true)
    private String cancellationReason;

    @Column(name = "payment_completed_at", nullable = true)
    private LocalDateTime PaymentCompletedAt;
    
    @Column(name = "payment_failure_reason", nullable = true)
    private String paymentFailureReason;

    @Column(name = "disbursement_reference")
    private String disbursementReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "disbursement_status")
    private DisbursementStatus disbursementStatus;
    
    @Column(name = "disbursement_completed_at")
    private LocalDateTime disbursementCompletedAt;

    @Column(name = "created_at")
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = true)
    private Branches branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biker_id")
    private Bikers bikers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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


    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }
    
    // Helper method to add a payment to this order
    public void addPayment(Payment payment) {
        if (payment == null) {
            return;
        }
        this.payment = payment;
        payment.setOrder(this);
        
        // Update payment status if needed
        if (payment.getPaymentStatus() != null) {
            this.setPaymentStatus(payment.getPaymentStatus());
        }
    }
    
    // Helper method to add a transaction to this order
    public void addTransaction(MomoTransaction transaction) {
        if (transaction == null) {
            return;
        }
        transaction.setOrder(this);
        
        // Link transaction with payment if not already linked
        if (this.payment != null && transaction.getPayment() == null) {
            transaction.setPayment(this.payment);
            this.payment.setMomoTransaction(transaction);
        }
        
        // Update order status based on transaction status if needed
        if (transaction.getStatus() != null) {
            switch (transaction.getStatus()) {
                case SUCCESSFUL:
                    this.setPaymentStatus(PaymentStatus.PAID);
                    break;
                case FAILED:
                    this.setPaymentStatus(PaymentStatus.FAILED);
                    break;
                case PENDING:
                    this.setPaymentStatus(PaymentStatus.PENDING);
                    break;
                default:
                    break;
            }
        }
    }
    
    // Helper method to check if order is paid
    public boolean isPaid() {
        return this.paymentStatus == PaymentStatus.PAID;
    }
    
    // Helper method to check if order can be processed
    public boolean canBeProcessed() {
        return this.isPaid() && this.orderStatus != OrderStatus.CANCELLED;
    }
}
