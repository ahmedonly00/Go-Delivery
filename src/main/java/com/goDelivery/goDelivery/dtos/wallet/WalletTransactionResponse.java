package com.goDelivery.goDelivery.dtos.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionResponse {
    private String transactionId;
    private String walletId;
    private String referenceId; // Order ID, Top-up ID, etc.
    private String type; // CREDIT, DEBIT
    private String transactionType; // TOP_UP, PAYMENT, REFUND, WITHDRAWAL, REFERRAL_BONUS, etc.
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String currency;
    private String status; // PENDING, COMPLETED, FAILED, REVERSED
    private String description;
    private String remarks;
    private LocalDateTime transactionDate;
    private String paymentMethod;
    private String paymentReference;
    private String metadata; // JSON string for additional data
}
