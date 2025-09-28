package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.Feedback;
import com.goDelivery.goDelivery.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByOrder(Order order);
    List<Feedback> findByCustomer(Customer customer);
    boolean existsByOrder(Order order);
}
