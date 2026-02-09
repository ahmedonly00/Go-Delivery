package com.goDelivery.goDelivery.dtos.agreement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemDeliveryAgreementDTO {

    private Long id;
    private String version;
    private String agreementText;
    private String terms;
    private Float commissionPercentage;
    private LocalDateTime createdAt;
}
