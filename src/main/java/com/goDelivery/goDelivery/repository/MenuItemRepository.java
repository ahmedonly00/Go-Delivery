package com.goDelivery.goDelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.MenuItem;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Optional<MenuItem> findByItemId(Long itemId);
    Optional<MenuItem> findByItemName(String itemName);
}
