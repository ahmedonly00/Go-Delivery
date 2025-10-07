package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Bikers;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BikersRepository extends JpaRepository<Bikers, Long> {

    Optional<Bikers> findByBikerId(Long bikerId);
    boolean existsById(Bikers bikerId);
    

}
