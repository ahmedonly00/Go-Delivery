package com.goDelivery.goDelivery.util;

import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionUpdateRunner implements CommandLineRunner {

    private final RestaurantUsersRepository restaurantUsersRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if this is a permission update run
        if (args.length > 0 && args[0].equals("update-permissions")) {
            log.info("Starting permission update for all users...");
            
            // Update CASHIER users
            int cashiersUpdated = restaurantUsersRepository.updatePermissionsByRole(
                Roles.CASHIER, 
                "READ_ORDERS,UPDATE_ORDERS,PROCESS_PAYMENTS,DISBURSEMENT_COLLECTION,DISBURSEMENT_STATUS"
            );
            log.info("Updated {} CASHIER users", cashiersUpdated);
            
            // Update RESTAURANT_ADMIN users
            int adminsUpdated = restaurantUsersRepository.updatePermissionsByRole(
                Roles.RESTAURANT_ADMIN, 
                "FULL_ACCESS,DISBURSEMENT_COLLECTION,DISBURSEMENT_STATUS"
            );
            log.info("Updated {} RESTAURANT_ADMIN users", adminsUpdated);
            
            // Update SUPER_ADMIN users
            int superAdminsUpdated = restaurantUsersRepository.updatePermissionsByRole(
                Roles.SUPER_ADMIN, 
                "SUPER_ACCESS,DISBURSEMENT_COLLECTION,DISBURSEMENT_STATUS"
            );
            log.info("Updated {} SUPER_ADMIN users", superAdminsUpdated);
            
            // Update BIKER users
            int bikersUpdated = restaurantUsersRepository.updatePermissionsByRole(
                Roles.BIKER, 
                "READ_DELIVERIES,UPDATE_DELIVERIES"
            );
            log.info("Updated {} BIKER users", bikersUpdated);
            
            log.info("Permission update completed!");
            System.exit(0);
        }
    }
}
