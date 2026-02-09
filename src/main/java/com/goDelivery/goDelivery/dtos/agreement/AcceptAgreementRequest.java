package com.goDelivery.goDelivery.dtos.agreement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AcceptAgreementRequest {

    @NotNull(message = "Agreement version is required")
    private String agreementVersion;

    @NotNull(message = "Acceptance is required")
    private Boolean accepted;
}
