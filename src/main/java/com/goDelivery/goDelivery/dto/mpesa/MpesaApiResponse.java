package com.goDelivery.goDelivery.dto.mpesa;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MpesaApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
