package com.goDelivery.goDelivery.dtos.tracking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BikerInfo {

    private Long bikerId;
    private String name;
    private String phoneNumber;
}
