package com.goDelivery.goDelivery.service;

import com.goDelivery.goDelivery.dtos.cart.CartItemDTO;
import com.goDelivery.goDelivery.dtos.cart.ShoppingCartDTO;
import com.goDelivery.goDelivery.exception.CartOperationException;
import com.goDelivery.goDelivery.mapper.CartMapper;
import com.goDelivery.goDelivery.model.*;
import com.goDelivery.goDelivery.repository.CartItemRepository;
import com.goDelivery.goDelivery.repository.MenuItemRepository;
import com.goDelivery.goDelivery.repository.ShoppingCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private Customer customer;
    private MenuItem menuItem;
    private ShoppingCart cart;
    private CartItem cartItem;
    private CartItemDTO cartItemDTO;
    private ShoppingCartDTO cartDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("Test User");
        customer.setEmail("test@example.com");

        menuItem = new MenuItem();
        menuItem.setItemId(1L);
        menuItem.setItemName("Test Item");
        menuItem.setPrice(BigDecimal.valueOf(10.99));
        menuItem.setDescription("Test Description");

        cart = new ShoppingCart();
        cart.setId(1L);
        cart.setCustomer(customer);

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setMenuItem(menuItem);
        cartItem.setQuantity(1);
        cartItem.setCart(cart);

        cartItemDTO = CartItemDTO.builder()
                .id(1L)
                .menuItemId(1L)
                .menuItemName("Test Item")
                .price(10.99)
                .quantity(1)
                .build();

        cartDTO = ShoppingCartDTO.builder()
                .id(1L)
                .customerId(1L)
                .totalPrice(10.99)
                .items(Collections.singletonList(cartItemDTO))
                .build();
    }

    @Test
    void getCart_WhenCartExists_ReturnsCart() {
        // Arrange
        when(shoppingCartRepository.findByCustomer_CustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toShoppingCartDTO(cart)).thenReturn(cartDTO);

        // Act
        ShoppingCartDTO result = cartService.getCart(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1, result.getItems().size());
        verify(shoppingCartRepository).findByCustomer_CustomerId(1L);
    }

    @Test
    void addItemToCart_WhenCartDoesNotExist_CreatesNewCart() {
        // Arrange
        when(shoppingCartRepository.findByCustomer_CustomerId(1L)).thenReturn(Optional.empty());
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem));
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> {
            ShoppingCart savedCart = invocation.getArgument(0);
            savedCart.setId(1L);
            return savedCart;
        });
        when(cartMapper.toShoppingCartDTO(any(ShoppingCart.class))).thenReturn(cartDTO);

        // Act
        ShoppingCartDTO result = cartService.addItemToCart(1L, cartItemDTO);

        // Assert
        assertNotNull(result);
        verify(shoppingCartRepository).save(any(ShoppingCart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItemToCart_WhenMenuItemDoesNotExist_ThrowsException() {
        // Arrange
        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartOperationException.class, () -> {
            cartService.addItemToCart(1L, cartItemDTO);
        });
    }

    @Test
    void updateCartItem_WhenItemExists_UpdatesItem() {
        // Arrange
        when(shoppingCartRepository.findByCustomer_CustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartMapper.toShoppingCartDTO(any(ShoppingCart.class))).thenReturn(cartDTO);

        CartItemDTO updateDTO = CartItemDTO.builder()
                .quantity(2)
                .specialInstructions("Extra sauce")
                .build();

        // Act
        ShoppingCartDTO result = cartService.updateCartItem(1L, 1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(2, cartItem.getQuantity());
        assertEquals("Extra sauce", cartItem.getSpecialInstructions());
    }

    @Test
    void removeItemFromCart_WhenItemExists_RemovesItem() {
        // Arrange
        cart.addItem(cartItem);
        when(shoppingCartRepository.findByCustomer_CustomerId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItem));
        when(cartMapper.toShoppingCartDTO(any(ShoppingCart.class))).thenReturn(cartDTO);

        // Act
        ShoppingCartDTO result = cartService.removeItemFromCart(1L, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty());
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void clearCart_WhenCartExists_ClearsAllItems() {
        // Arrange
        cart.addItem(cartItem);
        when(shoppingCartRepository.findByCustomer_CustomerId(1L)).thenReturn(Optional.of(cart));

        // Act
        cartService.clearCart(1L);

        // Assert
        assertTrue(cart.getItems().isEmpty());
        verify(shoppingCartRepository).save(cart);
    }
}
