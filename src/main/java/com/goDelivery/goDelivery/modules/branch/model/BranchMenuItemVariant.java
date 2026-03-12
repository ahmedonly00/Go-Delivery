package com.goDelivery.goDelivery.modules.branch.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "branch_menu_item_variant")
public class BranchMenuItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "price_modifier", nullable = false)
    private Float priceModifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_menu_item_id", nullable = false)
    private BranchMenuItem menuItem;
}
