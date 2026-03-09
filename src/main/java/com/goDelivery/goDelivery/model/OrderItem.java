package com.goDelivery.goDelivery.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Float unitPrice;

    @Column(name = "total_price", nullable = false)
    private Float totalPrice;

    @Column(name = "special_requests")
    private String specialRequests;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    // Many Order Items belong to One Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Many Order Items reference One Menu Item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    // Many Order Items can reference One Branch Menu Item (for branch orders)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_menu_item_id")
    private BranchMenuItem branchMenuItem;

    // Many Order Items can reference One Variant (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private MenuItemVariant variant;


}
