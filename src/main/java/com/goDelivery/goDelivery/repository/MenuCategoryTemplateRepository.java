package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.MenuCategoryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuCategoryTemplateRepository extends JpaRepository<MenuCategoryTemplate, Long> {
    Optional<MenuCategoryTemplate> findByCategoryName(String categoryName);
    List<MenuCategoryTemplate> findByIsActiveTrue();
}
