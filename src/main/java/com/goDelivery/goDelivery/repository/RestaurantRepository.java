package com.goDelivery.goDelivery.repository;

import com.goDelivery.goDelivery.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
       boolean existsByEmail(String email);

       Optional<Restaurant> findByRestaurantId(Long restaurantId);

       Optional<Restaurant> findByEmail(String email);

       @Query("SELECT r FROM Restaurant r WHERE r.isApproved = true AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL")
       List<Restaurant> findByIsApprovedTrueAndLatitudeIsNotNullAndLongitudeIsNotNull();

       @Query("SELECT r FROM Restaurant r WHERE LOWER(r.restaurantName) LIKE LOWER(concat('%', :name, '%')) " +
                     "AND r.isApproved = true AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL")
       List<Restaurant> findByRestaurantNameContainingIgnoreCaseAndIsApprovedTrueAndLatitudeIsNotNullAndLongitudeIsNotNull(
                     @Param("name") String name);

       boolean existsByRestaurantIdAndEmail(Long restaurantId, String email);

       List<Restaurant> findByLocation(String location);

       List<Restaurant> findByCuisineType(String cuisineType);

       List<Restaurant> findByIsActive(boolean isActive);

       List<Restaurant> findByCuisineTypeAndIsActive(String cuisineType, boolean isActive);

       @Query("SELECT r FROM Restaurant r WHERE " +
                     "LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                     "LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                     "LOWER(r.location) LIKE LOWER(CONCAT('%', :query, '%'))")
       List<Restaurant> searchRestaurants(@Param("query") String query);

       @Query("SELECT DISTINCT r FROM Restaurant r " +
                     "LEFT JOIN r.promotions p " +
                     "WHERE p IS NOT NULL AND p.isActive = true")
       List<Restaurant> findRestaurantsWithActivePromotions();

       @Query("SELECT r FROM Restaurant r WHERE r.rating >= :minRating")
       List<Restaurant> findRestaurantsWithMinRating(@Param("minRating") float minRating);

       // Approval-related queries
       List<Restaurant> findByApprovalStatus(com.goDelivery.goDelivery.Enum.ApprovalStatus approvalStatus);

       List<Restaurant> findByIsApprovedTrue();

       List<Restaurant> findByIsApprovedTrueAndIsActiveTrue();

       @Query("SELECT r FROM Restaurant r WHERE r.approvalStatus = com.goDelivery.goDelivery.Enum.ApprovalStatus.PENDING ORDER BY r.createdAt ASC")
       List<Restaurant> findPendingRestaurants();

       // ============ Dashboard Aggregation Queries ============

       // Count restaurants by approval status
       Long countByIsApprovedTrue();

       Long countByApprovalStatus(com.goDelivery.goDelivery.Enum.ApprovalStatus approvalStatus);

       // Count restaurants by date range
       @Query("SELECT COUNT(r) FROM Restaurant r WHERE r.createdAt BETWEEN :startDate AND :endDate")
       Long countRestaurantsByDateRange(@Param("startDate") java.time.LocalDate startDate,
                     @Param("endDate") java.time.LocalDate endDate);

       // Get average restaurant rating
       @Query("SELECT AVG(r.rating) FROM Restaurant r WHERE r.isApproved = true")
       Double getAverageRestaurantRating();

       // Count restaurants by cuisine type
       @Query("SELECT r.cuisineType, COUNT(r) FROM Restaurant r WHERE r.isApproved = true GROUP BY r.cuisineType")
       List<Object[]> countRestaurantsByCuisineType();

       // Get top rated restaurants
       @Query("SELECT r FROM Restaurant r WHERE r.isApproved = true ORDER BY r.rating DESC")
       List<Restaurant> findTopRatedRestaurants(org.springframework.data.domain.Pageable pageable);

}
