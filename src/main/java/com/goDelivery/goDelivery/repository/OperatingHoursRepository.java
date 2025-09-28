package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.OperatingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperatingHoursRepository extends JpaRepository<OperatingHours, Long> {
    OperatingHours findByRestaurantRestaurantId(Long restaurantId);
}
