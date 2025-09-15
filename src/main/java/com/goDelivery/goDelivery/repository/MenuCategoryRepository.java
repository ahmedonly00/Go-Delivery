package com.goDelivery.goDelivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goDelivery.goDelivery.model.MenuCategory;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    Optional<MenuCategory> findByCategoryId(Long categoryId);
    Optional<MenuCategory> findByCategoryName(String categoryName);
}
