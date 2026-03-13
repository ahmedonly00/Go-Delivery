package com.goDelivery.goDelivery.modules.delivery.repository;

import com.goDelivery.goDelivery.modules.delivery.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByCityName(String cityName);
    List<City> findByCountry_CountryId(Long countryId);
}
