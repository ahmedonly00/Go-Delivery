package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "menu_item_variant")
public class MenuItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "price_modifier", nullable = false)
    private  Float priceModifier;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItem menuItem;

    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;


}
