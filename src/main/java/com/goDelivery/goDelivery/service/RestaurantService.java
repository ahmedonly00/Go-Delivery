package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.Enum.DeliveryType;
import com.goDelivery.goDelivery.Enum.Roles;
import com.goDelivery.goDelivery.dtos.restaurant.*;
import com.goDelivery.goDelivery.exception.ResourceNotFoundException;
import com.goDelivery.goDelivery.mapper.RestaurantMapper;
import com.goDelivery.goDelivery.model.OperatingHours;
import com.goDelivery.goDelivery.model.Restaurant;
import com.goDelivery.goDelivery.model.RestaurantUsers;
import com.goDelivery.goDelivery.repository.OperatingHoursRepository;
import com.goDelivery.goDelivery.repository.OrderRepository;
import com.goDelivery.goDelivery.repository.RestaurantRepository;
import com.goDelivery.goDelivery.repository.RestaurantUsersRepository;
import com.goDelivery.goDelivery.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final OperatingHoursRepository operatingHoursRepository;
    private final RestaurantMapper restaurantMapper;
    private final EmailService emailService;
    private final RestaurantUsersRepository restaurantUsersRepository;
    private final OrderRepository orderRepository;
    private final GeocodingService geocodingService;
    private final GeoLocationService geoLocationService;

    public RestaurantDTO registerRestaurant(RestaurantDTO restaurantDTO) {
        Restaurant restaurant = restaurantMapper.toRestaurantForCreate(restaurantDTO);

        // Automatically geocode the restaurant address
        geocodeRestaurantLocation(restaurant);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toRestaurantDTO(savedRestaurant);
    }

    public RestaurantDTO updateRestaurant(Long restaurantId, RestaurantDTO restaurantDTO) {
        Restaurant existingRestaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        Double currentRating = existingRestaurant.getRating();
        Integer currentTotalReviews = existingRestaurant.getTotalReviews();
        String oldLocation = existingRestaurant.getLocation();

        restaurantMapper.toRestaurantForUpdate(existingRestaurant, restaurantDTO);

        existingRestaurant.setRating(currentRating);
        existingRestaurant.setTotalReviews(currentTotalReviews);

        // Re-geocode if location changed
        if (!oldLocation.equals(existingRestaurant.getLocation())) {
            log.info("Restaurant location changed from '{}' to '{}', re-geocoding...",
                    oldLocation, existingRestaurant.getLocation());
            geocodeRestaurantLocation(existingRestaurant);
        }

        Restaurant updatedRestaurant = restaurantRepository.save(existingRestaurant);
        return restaurantMapper.toRestaurantDTO(updatedRestaurant);
    }

    public boolean isUserRestaurantAdmin(String username, Long restaurantId) {
        return restaurantRepository.existsByRestaurantIdAndEmail(restaurantId, username);
    }

    public List<RestaurantDTO> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        return restaurantMapper.toRestaurantDTO(restaurants);
    }

    public RestaurantDTO updateRestaurantLogo(Long restaurantId, String logoUrl) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        restaurant.setLogoUrl(logoUrl);
        restaurant.setUpdatedAt(LocalDate.now());
        return restaurantMapper.toRestaurantDTO(restaurantRepository.save(restaurant));
    }

    public List<RestaurantDTO> getRestaurantsByLocation(String location) {
        List<Restaurant> restaurants = restaurantRepository.findByLocation(location);
        return restaurantMapper.toRestaurantDTO(restaurants);
    }

    public List<RestaurantDTO> getRestaurantsByCuisineType(String cuisineType) {
        List<Restaurant> restaurants = restaurantRepository.findByCuisineType(cuisineType);
        return restaurantMapper.toRestaurantDTO(restaurants);
    }

    // Search and filter restaurants based on various criteria
    public List<RestaurantDTO> searchRestaurants(RestaurantSearchRequest searchRequest) {
        // Start with all active restaurants
        List<Restaurant> restaurants = restaurantRepository.findByIsActive(true);

        // Apply filters
        if (searchRequest.getLocation() != null && !searchRequest.getLocation().isEmpty()) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getLocation().equalsIgnoreCase(searchRequest.getLocation()))
                    .collect(Collectors.toList());
        }

        if (searchRequest.getRestaurantName() != null && !searchRequest.getRestaurantName().isEmpty()) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getRestaurantName().equalsIgnoreCase(searchRequest.getRestaurantName()))
                    .collect(Collectors.toList());
        }

        if (searchRequest.getCuisineType() != null && !searchRequest.getCuisineType().isEmpty()) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getCuisineType().equalsIgnoreCase(searchRequest.getCuisineType()))
                    .collect(Collectors.toList());
        }

        if (searchRequest.getMinRating() != null) {
            restaurants = restaurants.stream()
                    .filter(r -> r.getRating() != null && r.getRating() >= searchRequest.getMinRating())
                    .collect(Collectors.toList());
        }

        // Apply sorting
        if (searchRequest.getSortBy() != null && !searchRequest.getSortBy().isEmpty()) {
            switch (searchRequest.getSortBy().toLowerCase()) {
                case "rating":
                    restaurants.sort((r1, r2) -> Double.compare(
                            r2.getRating() != null ? r2.getRating() : 0,
                            r1.getRating() != null ? r1.getRating() : 0));
                    break;
                case "popularity":
                    restaurants.sort((r1, r2) -> {
                        long r1Orders = orderRepository.countByRestaurant_RestaurantId(r1.getRestaurantId());
                        long r2Orders = orderRepository.countByRestaurant_RestaurantId(r2.getRestaurantId());
                        return Long.compare(r2Orders, r1Orders); // Descending order (most popular first)
                    });
                    break;
            }
        }

        return restaurants.stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }

    public RestaurantDTO getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        return restaurantMapper.toRestaurantDTO(restaurant);
    }

    public RestaurantDTO updateOperatingHours(Long restaurantId, UpdateOperatingHoursRequest request) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        OperatingHours operatingHours = restaurant.getOperatingHours();
        if (operatingHours == null) {
            operatingHours = new OperatingHours();
            operatingHours.setRestaurant(restaurant);
        }

        // Initialize all days as closed first
        operatingHours.setMondayOpen(null);
        operatingHours.setMondayClose(null);
        operatingHours.setTuesdayOpen(null);
        operatingHours.setTuesdayClose(null);
        operatingHours.setWednesdayOpen(null);
        operatingHours.setWednesdayClose(null);
        operatingHours.setThursdayOpen(null);
        operatingHours.setThursdayClose(null);
        operatingHours.setFridayOpen(null);
        operatingHours.setFridayClose(null);
        operatingHours.setSaturdayOpen(null);
        operatingHours.setSaturdayClose(null);
        operatingHours.setSundayOpen(null);
        operatingHours.setSundayClose(null);

        // Update operating hours from request
        if (request.getTimeSlots() != null) {
            for (UpdateOperatingHoursRequest.TimeSlot timeSlot : request.getTimeSlots()) {
                if (timeSlot.isOpen() && timeSlot.getDayOfWeek() != null) {
                    String openTime = timeSlot.getOpenTime() != null ? timeSlot.getOpenTime().toString() : null;
                    String closeTime = timeSlot.getCloseTime() != null ? timeSlot.getCloseTime().toString() : null;

                    switch (timeSlot.getDayOfWeek()) {
                        case MONDAY:
                            operatingHours.setMondayOpen(openTime);
                            operatingHours.setMondayClose(closeTime);
                            break;
                        case TUESDAY:
                            operatingHours.setTuesdayOpen(openTime);
                            operatingHours.setTuesdayClose(closeTime);
                            break;
                        case WEDNESDAY:
                            operatingHours.setWednesdayOpen(openTime);
                            operatingHours.setWednesdayClose(closeTime);
                            break;
                        case THURSDAY:
                            operatingHours.setThursdayOpen(openTime);
                            operatingHours.setThursdayClose(closeTime);
                            break;
                        case FRIDAY:
                            operatingHours.setFridayOpen(openTime);
                            operatingHours.setFridayClose(closeTime);
                            break;
                        case SATURDAY:
                            operatingHours.setSaturdayOpen(openTime);
                            operatingHours.setSaturdayClose(closeTime);
                            break;
                        case SUNDAY:
                            operatingHours.setSundayOpen(openTime);
                            operatingHours.setSundayClose(closeTime);
                            break;
                    }
                }
            }
        }
        // Save the updated operating hours
        operatingHours = operatingHoursRepository.save(operatingHours);
        restaurant.setOperatingHours(operatingHours);
        restaurant.setUpdatedAt(LocalDate.now());
        restaurant = restaurantRepository.save(restaurant);

        return restaurantMapper.toRestaurantDTO(restaurant);

    }

    /**
     * Find nearby approved restaurants based on customer location
     * Uses GeoLocationService to filter by distance and delivery radius
     */
    public List<Restaurant> findNearbyApprovedRestaurants(double latitude, double longitude, double radiusKm) {
        log.info("Finding nearby approved restaurants for location ({}, {}) within {} km",
                latitude, longitude, radiusKm);
        return geoLocationService.findNearbyRestaurants(latitude, longitude, radiusKm);
    }

    public List<RestaurantDTO> getAllActiveRestaurants() {
        return restaurantRepository.findByIsActive(true).stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantDTO> getRestaurantsByCuisine(String cuisineType) {
        return restaurantRepository.findByCuisineTypeAndIsActive(cuisineType, true).stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }

    // Business Document Update Methods
    public void updateCommercialRegistrationCertificate(Long restaurantId, String certificateUrl) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        restaurant.setCommercialRegistrationCertificateUrl(certificateUrl);
        restaurant.setUpdatedAt(LocalDate.now());
        restaurantRepository.save(restaurant);
    }

    public void updateTaxIdentificationNumber(Long restaurantId, String taxIdentificationNumber) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        restaurant.setTaxIdentificationNumber(taxIdentificationNumber);
        restaurant.setUpdatedAt(LocalDate.now());
        restaurantRepository.save(restaurant);
    }

    public void updateTaxIdentificationDocument(Long restaurantId, String documentUrl) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        restaurant.setTaxIdentificationDocumentUrl(documentUrl);
        restaurant.setUpdatedAt(LocalDate.now());
        restaurantRepository.save(restaurant);
    }

    // Restaurant Approval Methods
    public List<RestaurantDTO> getPendingRestaurants() {
        return restaurantRepository.findPendingRestaurants().stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }

    public List<RestaurantReviewDTO> getPendingRestaurantsForReview() {
        return restaurantMapper.toRestaurantReviewDTOList(restaurantRepository.findPendingRestaurants());
    }

    public RestaurantReviewDTO getRestaurantForReview(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));
        return restaurantMapper.toRestaurantReviewDTO(restaurant);
    }

    public List<RestaurantDTO> getRestaurantsByApprovalStatus(com.goDelivery.goDelivery.Enum.ApprovalStatus status) {
        return restaurantRepository.findByApprovalStatus(status).stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }

    public RestaurantDTO approveRestaurant(Long restaurantId, String reviewerEmail) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        restaurant.setIsApproved(true);
        restaurant.setIsActive(true); // Activate the restaurant when approved
        restaurant.setApprovalStatus(com.goDelivery.goDelivery.Enum.ApprovalStatus.APPROVED);
        restaurant.setReviewedBy(reviewerEmail);
        restaurant.setReviewedAt(LocalDate.now());
        restaurant.setRejectionReason(null);
        restaurant.setUpdatedAt(LocalDate.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // Send approval email to restaurant admin
        try {
            RestaurantUsers restaurantAdmin = restaurantUsersRepository
                    .findByRestaurantIdAndRole(restaurantId, Roles.RESTAURANT_ADMIN)
                    .orElse(null);

            if (restaurantAdmin != null) {
                emailService.sendRestaurantApprovalEmail(
                        restaurantAdmin.getEmail(),
                        restaurantAdmin.getFullName(),
                        restaurant.getRestaurantName());
                log.info("Approval email sent to restaurant admin: {}", restaurantAdmin.getEmail());
            } else {
                log.warn("No restaurant admin found for restaurant ID: {}", restaurantId);
            }
        } catch (Exception e) {
            log.error("Failed to send approval email for restaurant ID {}: {}", restaurantId, e.getMessage());
        }

        return restaurantMapper.toRestaurantDTO(savedRestaurant);
    }

    public RestaurantDTO rejectRestaurant(Long restaurantId, String rejectionReason, String reviewerEmail) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        restaurant.setIsApproved(false);
        restaurant.setApprovalStatus(com.goDelivery.goDelivery.Enum.ApprovalStatus.REJECTED);
        restaurant.setRejectionReason(rejectionReason);
        restaurant.setReviewedBy(reviewerEmail);
        restaurant.setReviewedAt(LocalDate.now());
        restaurant.setIsActive(false); // Always deactivate rejected restaurants
        restaurant.setUpdatedAt(LocalDate.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // Send rejection email to restaurant admin
        try {
            RestaurantUsers restaurantAdmin = restaurantUsersRepository
                    .findByRestaurantIdAndRole(restaurantId, Roles.RESTAURANT_ADMIN)
                    .orElse(null);

            if (restaurantAdmin != null) {
                emailService.sendRestaurantRejectionEmail(
                        restaurantAdmin.getEmail(),
                        restaurantAdmin.getFullName(),
                        restaurant.getRestaurantName(),
                        rejectionReason);
                log.info("Rejection email sent to restaurant admin: {}", restaurantAdmin.getEmail());
            } else {
                log.warn("No restaurant admin found for restaurant ID: {}", restaurantId);
            }
        } catch (Exception e) {
            log.error("Failed to send rejection email for restaurant ID {}: {}", restaurantId, e.getMessage());
            // Don't fail the rejection if email fails
        }

        return restaurantMapper.toRestaurantDTO(savedRestaurant);
    }

    public List<RestaurantDTO> getApprovedRestaurants() {
        log.debug("Fetching approved and active restaurants");
        List<Restaurant> restaurants = restaurantRepository.findByIsApprovedTrueAndIsActiveTrue();
        log.debug("Found {} approved and active restaurants", restaurants.size());
        if (!restaurants.isEmpty()) {
            log.debug("First restaurant: {}", restaurants.get(0).getRestaurantName());
        }
        return restaurants.stream()
                .map(restaurantMapper::toRestaurantDTO)
                .collect(Collectors.toList());
    }

    public RestaurantDTO updateDeliverySettings(Long restaurantId, DeliverySettingsRequest request) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        // Update delivery type
        restaurant.setDeliveryType(request.getDeliveryType());

        // Handle SELF_DELIVERY settings
        if (request.getDeliveryType() == DeliveryType.SELF_DELIVERY) {
            // Validation is handled by @ValidDeliverySettings annotation
            restaurant.setRadiusUnit(request.getRadiusUnit());
            restaurant.setBaseDeliveryFee(request.getBaseDeliveryFee());
            restaurant.setPerKmFee(request.getPerKmFee());
            restaurant.setDeliveryRadius(request.getDeliveryRadius());

            // Keep old deliveryFee for backward compatibility
            restaurant.setDeliveryFee(request.getBaseDeliveryFee());
        } else if (request.getDeliveryType() == DeliveryType.SYSTEM_DELIVERY) {
            // Validation is handled by @ValidDeliverySettings annotation
            // Clear self-delivery fields
            restaurant.setRadiusUnit(null);
            restaurant.setBaseDeliveryFee(null);
            restaurant.setPerKmFee(null);
            restaurant.setDeliveryRadius(null);
        }

        restaurant.setUpdatedAt(LocalDate.now());
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return restaurantMapper.toRestaurantDTO(savedRestaurant);
    }

    /**
     * Automatically geocode restaurant location to get latitude and longitude
     * This allows restaurants to appear in nearby searches
     */
    private void geocodeRestaurantLocation(Restaurant restaurant) {
        if (restaurant.getLocation() == null || restaurant.getLocation().trim().isEmpty()) {
            log.warn("Restaurant {} has no location address, skipping geocoding",
                    restaurant.getRestaurantId());
            return;
        }

        try {
            log.info("Geocoding restaurant location: {}", restaurant.getLocation());
            com.goDelivery.goDelivery.model.Coordinates coords = geocodingService
                    .geocodeAddress(restaurant.getLocation());

            restaurant.setLatitude(coords.getLatitude());
            restaurant.setLongitude(coords.getLongitude());

            log.info("Successfully geocoded restaurant '{}' to coordinates: {}, {}",
                    restaurant.getRestaurantName(),
                    coords.getLatitude(),
                    coords.getLongitude());
        } catch (Exception e) {
            log.error("Failed to geocode restaurant location '{}': {}. " +
                    "Restaurant will not appear in location-based searches.",
                    restaurant.getLocation(), e.getMessage());
            // Don't throw exception - allow restaurant registration to continue
            // even if geocoding fails
        }
    }

    /**
     * Geocode all restaurants that don't have coordinates
     * Useful for migrating existing restaurants
     */
    public Map<String, Object> geocodeAllRestaurantsWithoutCoordinates() {
        List<Restaurant> restaurantsWithoutCoords = restaurantRepository
                .findAll()
                .stream()
                .filter(r -> r.getLatitude() == null || r.getLongitude() == null)
                .collect(Collectors.toList());

        log.info("Found {} restaurants without coordinates", restaurantsWithoutCoords.size());

        int successCount = 0;
        int failCount = 0;

        for (Restaurant restaurant : restaurantsWithoutCoords) {
            try {
                geocodeRestaurantLocation(restaurant);
                if (restaurant.getLatitude() != null && restaurant.getLongitude() != null) {
                    restaurantRepository.save(restaurant);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("Error geocoding restaurant {}: {}", restaurant.getRestaurantId(), e.getMessage());
                failCount++;
            }
        }

        log.info("Geocoding complete. Success: {}, Failed: {}", successCount, failCount);

        return Map.of(
                "total", restaurantsWithoutCoords.size(),
                "success", successCount,
                "failed", failCount,
                "message",
                String.format("Geocoded %d out of %d restaurants", successCount, restaurantsWithoutCoords.size()));
    }

    /**
     * Manually geocode a specific restaurant by ID
     */
    public void geocodeRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findByRestaurantId(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + restaurantId));

        geocodeRestaurantLocation(restaurant);

        if (restaurant.getLatitude() == null || restaurant.getLongitude() == null) {
            throw new RuntimeException("Failed to geocode restaurant. Check logs for details.");
        }

        restaurantRepository.save(restaurant);
    }
}
