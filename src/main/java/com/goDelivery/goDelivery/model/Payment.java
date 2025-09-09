package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.PaymentMenthod;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
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
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMenthod paymentMenthod;

    @Column(name = "payment_provider", nullable = false)
    private String paymentProvider;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "reference_number", nullable = false)
    private String referenceNumber;

    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "gate_way_response", nullable = false)
    private String gateWayResponse;

    @Column(name = "failure_reason", nullable = false)
    private String failureReason;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // One Payment belongs to One Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
