package com.goDelivery.goDelivery.dtos.wallet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private String walletId;
    private Long userId;
    private BigDecimal balance;
    private String currency;
    private boolean isActive;
    private String status;
    private String lastTransactionId;
    private String lastTransactionType;
    private String lastTransactionAmount;
    private String lastTransactionDate;
}
