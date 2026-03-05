package com.goDelivery.goDelivery.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branch_menu_item")
public class BranchMenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "menu_item_name", nullable = false)
    private String menuItemName;

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private Float price;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "ingredients")
    private String ingredients;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @Column(name = "preparation_time")
    private Integer preparationTime;

    @Column(name = "preparation_score")
    private Integer preparationScore;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    /** Tracks which restaurant MenuItem this was inherited from, if any. */
    @Column(name = "source_restaurant_item_id")
    private Long sourceRestaurantItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branches branch;

    @JsonBackReference("branch-category-items")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private BranchMenuCategory category;

    @Builder.Default
    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<BranchMenuItemVariant> variants = new ArrayList<>();
}
