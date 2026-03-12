package com.goDelivery.goDelivery.modules.ordering.repository;

import com.goDelivery.goDelivery.modules.ordering.model.CartItem;
import com.goDelivery.goDelivery.model.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(ShoppingCart cart);
    List<CartItem> findByMenuItem(MenuItem menuItem);

}
