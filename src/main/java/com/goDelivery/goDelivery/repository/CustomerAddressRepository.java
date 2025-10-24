package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByCustomerCustomerId(Long customerId);
    List<CustomerAddress> findByCustomerCustomerIdOrderByIsDefaultDesc(Long customerId);
}
