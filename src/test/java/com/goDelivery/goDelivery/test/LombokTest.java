package com.goDelivery.goDelivery.test;

import com.goDelivery.goDelivery.model.MenuCategory;

/**
 * Test class to verify Lombok is generating correct getters/setters
 */
public class LombokTest {
    public static void main(String[] args) {
        MenuCategory category = new MenuCategory();
        
        // Test isActive field
        category.setActive(true);  // Should work if Lombok generates setActive()
        boolean active = category.isActive();  // Should work if Lombok generates isActive()
        
        System.out.println("MenuCategory active: " + active);
        
        // Test MenuItem isAvailable
        com.goDelivery.goDelivery.model.MenuItem item = new com.goDelivery.goDelivery.model.MenuItem();
        item.setAvailable(true);  // Should work if Lombok generates setAvailable()
        boolean available = item.isAvailable();  // Should work if Lombok generates isAvailable()
        
        System.out.println("MenuItem available: " + available);
    }
}
