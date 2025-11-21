package com.goDelivery.goDelivery.dtos.momo;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkDisbursementRequest {
    
    private String callback;
    
    @NotEmpty(message = "Recipients list cannot be empty")
    private List<DisbursementRequest> recipients;
}
