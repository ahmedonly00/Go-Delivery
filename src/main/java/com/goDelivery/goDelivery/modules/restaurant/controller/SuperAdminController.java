package com.goDelivery.goDelivery.modules.restaurant.controller;

import com.goDelivery.goDelivery.shared.enums.ApprovalStatus;
import com.goDelivery.goDelivery.shared.enums.StatsPeriod;
import com.goDelivery.goDelivery.modules.restaurant.dto.CreateSuperAdminRequest;
import com.goDelivery.goDelivery.modules.restaurant.dto.SystemStatsDTO;
import com.goDelivery.goDelivery.modules.delivery.dto.BikerDetailsResponse;
import com.goDelivery.goDelivery.modules.delivery.dto.BikerUpdateRequest;
import com.goDelivery.goDelivery.modules.restaurant.dto.BranchesDTO;
import com.goDelivery.goDelivery.modules.restaurant.dto.RestaurantDTO;
import com.goDelivery.goDelivery.modules.restaurant.model.SuperAdmin;
import com.goDelivery.goDelivery.modules.branch.service.BranchService;
import com.goDelivery.goDelivery.modules.restaurant.service.RestaurantService;
import com.goDelivery.goDelivery.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Super Admin", description = "Super admin management")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final BranchService branchService;
    private final RestaurantService restaurantService;

    @PostMapping(value = "/registerSuperAdmin")
    public ResponseEntity<SuperAdmin> registerSuperAdmin(
            @Valid @RequestBody CreateSuperAdminRequest request) {

        SuperAdmin superAdmin = superAdminService.createSuperAdmin(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(superAdmin.getAdminId())
                .toUri();

        return ResponseEntity.created(location).body(superAdmin);
    }

    @GetMapping(value = "/getAllSuperAdmins")
    public ResponseEntity<List<SuperAdmin>> getAllSuperAdmins() {
        return ResponseEntity.ok(superAdminService.getAllSuperAdmins());
    }

    @GetMapping(value = "/getAllBikers")
    public ResponseEntity<List<BikerDetailsResponse>> getAllBikers() {
        List<BikerDetailsResponse> bikers = superAdminService.getAllBikers();
        return ResponseEntity.ok(bikers);
    }

    @GetMapping(value = "/getActiveBikers")
    public ResponseEntity<List<BikerDetailsResponse>> getActiveBikers() {
        List<BikerDetailsResponse> activeBikers = superAdminService.getActiveBikers();
        return ResponseEntity.ok(activeBikers);
    }

    @GetMapping(value = "/getBikerById/{bikerId}")
    public ResponseEntity<BikerDetailsResponse> getBikerById(@PathVariable Long bikerId) {
        BikerDetailsResponse biker = superAdminService.getBikerById(bikerId);
        return ResponseEntity.ok(biker);
    }

    @PutMapping(value = "/updateBiker/{bikerId}")
    public ResponseEntity<BikerDetailsResponse> updateBiker(
            @PathVariable Long bikerId,
            @Valid @RequestBody BikerUpdateRequest updateRequest) {
        BikerDetailsResponse updatedBiker = superAdminService.updateBiker(bikerId, updateRequest);
        return ResponseEntity.ok(updatedBiker);
    }

    @DeleteMapping(value = "/deleteBiker/{bikerId}")
    public ResponseEntity<Void> deleteBiker(@PathVariable Long bikerId) {
        superAdminService.deleteBiker(bikerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/activateBiker/{bikerId}")
    public ResponseEntity<Void> activateBiker(@PathVariable Long bikerId) {
        superAdminService.activateBiker(bikerId, true);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/deactivateBiker/{bikerId}")
    public ResponseEntity<Void> deactivateBiker(@PathVariable Long bikerId) {
        superAdminService.activateBiker(bikerId, false);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/getSuperAdminById/{id}")
    public ResponseEntity<SuperAdmin> getSuperAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(superAdminService.getSuperAdminById(id));
    }

    @PutMapping(value = "/updateSuperAdmin/{id}")
    public ResponseEntity<SuperAdmin> updateSuperAdmin(@PathVariable Long id, @RequestBody SuperAdmin superAdmin) {
        return ResponseEntity.ok(superAdminService.updateSuperAdmin(id, superAdmin));
    }

    @DeleteMapping(value = "/deleteSuperAdmin/{id}")
    public ResponseEntity<Void> deleteSuperAdmin(@PathVariable Long id) {
        superAdminService.deleteSuperAdmin(id);
        return ResponseEntity.ok().build();
    }

    // ── System Stats ──────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get system-wide stats", description = "Returns restaurant counts, staff members, paid/confirmed orders and revenue. Use ?period=DAY, WEEK, YEAR, or ALL (default)")
    public ResponseEntity<SystemStatsDTO> getSystemStats(
            @RequestParam(defaultValue = "ALL") StatsPeriod period) {
        return ResponseEntity.ok(superAdminService.getSystemStats(period));
    }

    // ── Branch Management ─────────────────────────────────────────────────────

    @GetMapping("/branches")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all branches", description = "Paginated list of all branches across all restaurants. Filter by approvalStatus (PENDING, APPROVED, REJECTED). Sort with ?sort=branchName,asc")
    public ResponseEntity<Page<BranchesDTO>> getAllBranches(
            @RequestParam(required = false) ApprovalStatus status,
            @PageableDefault(size = 20, sort = "branchId") Pageable pageable) {
        if (status != null) {
            return ResponseEntity.ok(branchService.getAllBranchesByStatusPaged(status, pageable));
        }
        return ResponseEntity.ok(branchService.getAllBranchesPaged(pageable));
    }

    @GetMapping("/branches/{branchId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get branch details", description = "Get full details of a specific branch by ID")
    public ResponseEntity<BranchesDTO> getBranchById(@PathVariable Long branchId) {
        return ResponseEntity.ok(branchService.getBranchById(branchId));
    }

    @GetMapping("/branches/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get branches by restaurant", description = "Paginated list of all branches belonging to a specific restaurant")
    public ResponseEntity<Page<BranchesDTO>> getBranchesByRestaurant(
            @PathVariable Long restaurantId,
            @PageableDefault(size = 20, sort = "branchId") Pageable pageable) {
        return ResponseEntity.ok(branchService.getAllBranchesForRestaurantPaged(restaurantId, pageable));
    }

    // ── Restaurant Management ─────────────────────────────────────────────────

    @GetMapping("/restaurants")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get all restaurants", description = "Paginated list of all restaurants. Filter by approvalStatus (PENDING, APPROVED, REJECTED). Sort with ?sort=restaurantName,asc")
    public ResponseEntity<Page<RestaurantDTO>> getAllRestaurants(
            @RequestParam(required = false) ApprovalStatus status,
            @PageableDefault(size = 20, sort = "restaurantId") Pageable pageable) {
        if (status != null) {
            return ResponseEntity.ok(restaurantService.getRestaurantsByApprovalStatusPaged(status, pageable));
        }
        return ResponseEntity.ok(restaurantService.getAllRestaurantsPaged(pageable));
    }

}
