package com.goDelivery.goDelivery.mapper;

import org.springframework.stereotype.Component;
import com.goDelivery.goDelivery.dtos.restaurant.OperatingHoursDTO;
import com.goDelivery.goDelivery.model.OperatingHours;

@Component
public class OperatingHoursMapper {

    public OperatingHours toEntity(OperatingHoursDTO dto) {
        if (dto == null) {
            return null;
        }

        if (dto.isClosed()) {
            // If the restaurant is closed, set all times to null
            return createOperatingHoursWithAllDays(null, null);
        }

        // Apply the same open/close times to all days
        return createOperatingHoursWithAllDays(dto.getOpen(), dto.getClose());
    }

    public OperatingHoursDTO toDto(OperatingHours entity) {
        if (entity == null) {
            return null;
        }

        // Find the first non-null day's hours
        String openTime = findFirstNonNullOpenTime(entity);
        String closeTime = findFirstNonNullCloseTime(entity);

        return OperatingHoursDTO.builder()
                .open(openTime)
                .close(closeTime)
                .isClosed(openTime == null && closeTime == null)
                .build();
    }

    private OperatingHours createOperatingHoursWithAllDays(String openTime, String closeTime) {
        return OperatingHours.builder()
                .mondayOpen(openTime).mondayClose(closeTime)
                .tuesdayOpen(openTime).tuesdayClose(closeTime)
                .wednesdayOpen(openTime).wednesdayClose(closeTime)
                .thursdayOpen(openTime).thursdayClose(closeTime)
                .fridayOpen(openTime).fridayClose(closeTime)
                .saturdayOpen(openTime).saturdayClose(closeTime)
                .sundayOpen(openTime).sundayClose(closeTime)
                .build();
    }

    private String findFirstNonNullOpenTime(OperatingHours entity) {
        if (entity.getMondayOpen() != null) return entity.getMondayOpen();
        if (entity.getTuesdayOpen() != null) return entity.getTuesdayOpen();
        if (entity.getWednesdayOpen() != null) return entity.getWednesdayOpen();
        if (entity.getThursdayOpen() != null) return entity.getThursdayOpen();
        if (entity.getFridayOpen() != null) return entity.getFridayOpen();
        if (entity.getSaturdayOpen() != null) return entity.getSaturdayOpen();
        return entity.getSundayOpen();
    }

    private String findFirstNonNullCloseTime(OperatingHours entity) {
        if (entity.getMondayClose() != null) return entity.getMondayClose();
        if (entity.getTuesdayClose() != null) return entity.getTuesdayClose();
        if (entity.getWednesdayClose() != null) return entity.getWednesdayClose();
        if (entity.getThursdayClose() != null) return entity.getThursdayClose();
        if (entity.getFridayClose() != null) return entity.getFridayClose();
        if (entity.getSaturdayClose() != null) return entity.getSaturdayClose();
        return entity.getSundayClose();
    }
}
