package com.goDelivery.goDelivery.model;

import com.goDelivery.goDelivery.Enum.TransactionType;
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
@Table(name = "wallet_transaction")
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Float amount;

    @Column(name = "balanced_after", nullable = false)
    private Float balanceAfter;

    @Column(name = "description", nullable = false)
    private  String description;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // Many Transactions belong to One Wallet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    // Many Transactions can be related to One Order (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

}
