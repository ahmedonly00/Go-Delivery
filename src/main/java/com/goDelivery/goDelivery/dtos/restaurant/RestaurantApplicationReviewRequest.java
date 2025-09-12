package com.goDelivery.goDelivery.dtos.restaurant;

import com.goDelivery.goDelivery.Enum.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantApplicationReviewRequest {
 
    private ApplicationStatus applicationStatus;

    private String reviewNote;
    
    private LocalDate reviewedAt;

    private String rejectionReason;
    
    private LocalDate approvedAt;
    
    private Long reviewedById;


    @Override
    public String toString() {
        return "RestaurantApplicationReviewRequest{" +
                "applicationStatus=" + applicationStatus +
                ", reviewNote='" + reviewNote + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}
