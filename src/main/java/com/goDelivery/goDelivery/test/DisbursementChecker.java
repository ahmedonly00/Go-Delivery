package com.goDelivery.goDelivery.test;

import com.goDelivery.goDelivery.repository.DisbursementTransactionRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.model.Order;
import com.goDelivery.goDelivery.Enum.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DisbursementChecker implements CommandLineRunner {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private DisbursementTransactionRepository disbursementRepository;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== Checking Recent Orders and Disbursements ===");
        
        // Get recent paid orders
        List<Order> recentPaidOrders = orderRepository.findTop10ByOrderByUpdatedAtDesc();
        
        for (Order order : recentPaidOrders) {
            log.info("Order ID: {}, Status: {}, Payment Status: {}, Updated: {}", 
                    order.getOrderId(), 
                    order.getOrderStatus(), 
                    order.getPaymentStatus(),
                    order.getUpdatedAt());
            
            // Check if there are disbursements for this order
            var disbursements = disbursementRepository.findByOrder_OrderId(order.getOrderId());
            log.info("  - Number of disbursements: {}", disbursements.size());
            
            disbursements.forEach(d -> 
                log.info("    Disbursement ID: {}, Amount: {}, Status: {}, Created: {}", 
                        d.getReferenceId(), 
                        d.getAmount(), 
                        d.getStatus(),
                        d.getCreatedAt()));
        }
        
        log.info("=== End of Check ===");
    }
}
