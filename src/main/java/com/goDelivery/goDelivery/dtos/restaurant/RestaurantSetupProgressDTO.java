package com.goDelivery.goDelivery.dtos.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantSetupProgressDTO {
    private int totalSteps;
    private int completedSteps;
    private int currentStep;
    private int overallProgress; // percentage
    private List<StepStatusDTO> stepStatuses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepStatusDTO {
        private String step;
        private boolean completed;
        private String label;
        private String description;
    }
}
