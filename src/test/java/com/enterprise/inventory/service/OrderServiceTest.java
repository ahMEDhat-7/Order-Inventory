package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.request.OrderRequest;
import com.enterprise.inventory.dto.response.OrderResponse;
import com.enterprise.inventory.exception.InsufficientStockException;
import com.enterprise.inventory.model.Order;
import com.enterprise.inventory.model.Product;
import com.enterprise.inventory.model.User;
import com.enterprise.inventory.repository.OrderRepository;
import com.enterprise.inventory.repository.ProductRepository;
import com.enterprise.inventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_shouldDeductStockAndReturnOrder() {
        User user = User.builder().id(1L).email("test@example.com").role("ROLE_USER").build();
        Product product = Product.builder().id(1L).sku("PROD-001").name("Test")
                .price(BigDecimal.valueOf(10.00)).stockQuantity(10).build();
        Order savedOrder = Order.builder().id(1L).user(user).status("COMPLETED")
                .totalAmount(BigDecimal.valueOf(20.00)).build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderRequest request = new OrderRequest();
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        OrderResponse response = orderService.createOrder("test@example.com", request);

        assertEquals("COMPLETED", response.getStatus());
        assertEquals(8, product.getStockQuantity());
        verify(redisTemplate).delete("product:1");
    }

    @Test
    void createOrder_shouldThrowOnInsufficientStock() {
        User user = User.builder().id(1L).email("test@example.com").role("ROLE_USER").build();
        Product product = Product.builder().id(1L).sku("PROD-001").name("Test")
                .price(BigDecimal.TEN).stockQuantity(1).build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        OrderRequest request = new OrderRequest();
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(5);
        request.setItems(List.of(item));

        assertThrows(InsufficientStockException.class,
                () -> orderService.createOrder("test@example.com", request));

        verify(productRepository, never()).save(any());
    }

    @Test
    void getOrdersByUser_shouldReturnOrders() {
        User user = User.builder().id(1L).email("test@example.com").role("ROLE_USER").build();
        Order order = Order.builder().id(1L).user(user).status("COMPLETED")
                .totalAmount(BigDecimal.TEN).items(List.of()).build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(order));

        List<OrderResponse> responses = orderService.getOrdersByUser("test@example.com");

        assertEquals(1, responses.size());
        assertEquals("COMPLETED", responses.get(0).getStatus());
    }
}
