package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByCustomer(Customer customer);
    Optional<ShoppingCart> findByCustomer_CustomerId(Long customerId);
    boolean existsByCustomer(Customer customer);
}
