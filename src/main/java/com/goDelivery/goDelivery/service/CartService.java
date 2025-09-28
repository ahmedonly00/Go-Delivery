package com.goDelivery.goDelivery.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goDelivery.goDelivery.dtos.cart.CartItemDTO;
import com.goDelivery.goDelivery.dtos.cart.ShoppingCartDTO;
import com.goDelivery.goDelivery.exception.CartOperationException;
import com.goDelivery.goDelivery.mapper.CartMapper;
import com.goDelivery.goDelivery.model.CartItem;
import com.goDelivery.goDelivery.model.Customer;
import com.goDelivery.goDelivery.model.MenuItem;
import com.goDelivery.goDelivery.model.ShoppingCart;
import com.goDelivery.goDelivery.repository.CartItemRepository;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import com.goDelivery.goDelivery.repository.ShoppingCartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final CartMapper cartMapper;
    

    @Transactional(readOnly = true)
    public ShoppingCartDTO getCart(Long customerId) {
        log.debug("Fetching cart for customer ID: {}", customerId);
        
        ShoppingCart cart = shoppingCartRepository.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new CartOperationException("Shopping cart not found for customer ID: " + customerId));
        
        return cartMapper.toShoppingCartDTO(cart);
    }

    @Transactional
    public ShoppingCartDTO addItemToCart(Long customerId, CartItemDTO cartItemDTO) {
        log.info("Adding item to cart for customer ID: {}", customerId);
        
        validateCartItem(cartItemDTO);
        
        ShoppingCart cart = getOrCreateCart(customerId);
        MenuItem menuItem = getMenuItem(cartItemDTO.getMenuItemId());
        
        Optional<CartItem> existingItem = findCartItem(cart, menuItem.getMenuItemId());
        
        if (existingItem.isPresent()) {
            updateExistingCartItem(existingItem.get(), cartItemDTO);
        } else {
            addNewCartItem(cart, menuItem, cartItemDTO);
        }
        
        return cartMapper.toShoppingCartDTO(shoppingCartRepository.save(cart));
    }

    @Transactional
    public ShoppingCartDTO updateCartItem(Long customerId, Long itemId, CartItemDTO cartItemDTO) {
        log.info("Updating cart item {} for customer ID: {}", itemId, customerId);
        
        validateCartItem(cartItemDTO);
        
        ShoppingCart cart = getCustomerCart(customerId);
        CartItem cartItem = getCartItem(cart, itemId);
        
        updateCartItemDetails(cartItem, cartItemDTO);
        cartItemRepository.save(cartItem);
        
        return cartMapper.toShoppingCartDTO(shoppingCartRepository.save(cart));
    }

    @Transactional
    public ShoppingCartDTO removeItemFromCart(Long customerId, Long itemId) {
        log.info("Removing item {} from cart for customer ID: {}", itemId, customerId);
        
        ShoppingCart cart = getCustomerCart(customerId);
        CartItem cartItem = getCartItem(cart, itemId);
        
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        
        return cartMapper.toShoppingCartDTO(shoppingCartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long customerId) {
        log.info("Clearing cart for customer ID: {}", customerId);
        
        ShoppingCart cart = getCustomerCart(customerId);
        cart.getItems().clear();
        shoppingCartRepository.save(cart);
        
        log.info("Cart cleared successfully for customer ID: {}", customerId);
    }

    // Private helper methods
    private ShoppingCart getOrCreateCart(Long customerId) {
        return shoppingCartRepository.findByCustomer_CustomerId(customerId)
                .orElseGet(() -> createNewCart(customerId));
    }
    
    private ShoppingCart createNewCart(Long customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        return shoppingCartRepository.save(
            ShoppingCart.builder()
                .customer(customer)
                .build()
        );
    }
    
    private ShoppingCart getCustomerCart(Long customerId) {
        return shoppingCartRepository.findByCustomer_CustomerId(customerId)
                .orElseThrow(() -> new CartOperationException("Shopping cart not found for customer ID: " + customerId));
    }
    
    private MenuItem getMenuItem(Long menuItemId) {
        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new CartOperationException("Menu item not found with ID: " + menuItemId));
    }
    
    private Optional<CartItem> findCartItem(ShoppingCart cart, Long menuItemId) {
        return cart.getItems().stream()
                .filter(item -> item.getMenuItem().getMenuItemId().equals(menuItemId))
                .findFirst();
    }
    
    private CartItem getCartItem(ShoppingCart cart, Long itemId) {
        return cartItemRepository.findById(itemId)
                .filter(item -> item.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new CartOperationException("Cart item not found with ID: " + itemId));
    }
    
    private void validateCartItem(CartItemDTO cartItemDTO) {
        if (cartItemDTO.getQuantity() == null || cartItemDTO.getQuantity() <= 0) {
            throw new CartOperationException("Quantity must be greater than zero");
        }
    }
    
    private void updateExistingCartItem(CartItem existingItem, CartItemDTO cartItemDTO) {
        existingItem.setQuantity(existingItem.getQuantity() + cartItemDTO.getQuantity());
        if (cartItemDTO.getSpecialInstructions() != null) {
            existingItem.setSpecialInstructions(cartItemDTO.getSpecialInstructions());
        }
        cartItemRepository.save(existingItem);
    }
    
    private void addNewCartItem(ShoppingCart cart, MenuItem menuItem, CartItemDTO cartItemDTO) {
        CartItem newItem = CartItem.builder()
                .menuItem(menuItem)
                .quantity(cartItemDTO.getQuantity())
                .specialInstructions(cartItemDTO.getSpecialInstructions())
                .cart(cart)
                .build();
        cart.addItem(newItem);
        cartItemRepository.save(newItem);
    }
    
    private void updateCartItemDetails(CartItem cartItem, CartItemDTO cartItemDTO) {
        if (cartItemDTO.getQuantity() != null && cartItemDTO.getQuantity() > 0) {
            cartItem.setQuantity(cartItemDTO.getQuantity());
        }
        if (cartItemDTO.getSpecialInstructions() != null) {
            cartItem.setSpecialInstructions(cartItemDTO.getSpecialInstructions());
        }
    }
}
