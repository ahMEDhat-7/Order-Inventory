package com.enterprise.inventory.service;

import com.enterprise.inventory.dto.request.ProductRequest;
import com.enterprise.inventory.dto.response.ProductResponse;
import com.enterprise.inventory.model.Product;
import com.enterprise.inventory.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_shouldReturnResponse() {
        ProductRequest request = new ProductRequest();
        request.setSku("PROD-001");
        request.setName("Test Product");
        request.setPrice(BigDecimal.valueOf(99.99));
        request.setStockQuantity(10);

        Product savedProduct = Product.builder()
                .id(1L)
                .sku("PROD-001")
                .name("Test Product")
                .price(BigDecimal.valueOf(99.99))
                .stockQuantity(10)
                .version(0)
                .build();

        when(productRepository.existsBySku("PROD-001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertEquals("PROD-001", response.getSku());
        assertEquals("Test Product", response.getName());
    }

    @Test
    void createProduct_shouldThrowOnDuplicateSku() {
        ProductRequest request = new ProductRequest();
        request.setSku("DUPLICATE");
        request.setName("Test");
        request.setPrice(BigDecimal.TEN);
        request.setStockQuantity(1);

        when(productRepository.existsBySku("DUPLICATE")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));
    }

    @Test
    void getProductById_shouldReturnCachedProduct() {
        ProductResponse cached = ProductResponse.builder()
                .id(1L)
                .sku("PROD-001")
                .name("Cached Product")
                .price(BigDecimal.TEN)
                .stockQuantity(5)
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("product:1")).thenReturn(cached);

        ProductResponse response = productService.getProductById(1L);

        assertEquals("Cached Product", response.getName());
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    void getProductById_shouldFetchFromDbOnCacheMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("product:1")).thenReturn(null);

        Product product = Product.builder()
                .id(1L)
                .sku("PROD-001")
                .name("DB Product")
                .price(BigDecimal.TEN)
                .stockQuantity(5)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertEquals("DB Product", response.getName());
        verify(valueOperations).set(eq("product:1"), any(), anyLong(), any());
    }

    @Test
    void getAllProducts_shouldReturnList() {
        Product product = Product.builder()
                .id(1L)
                .sku("PROD-001")
                .name("Product 1")
                .price(BigDecimal.TEN)
                .stockQuantity(5)
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponse> responses = productService.getAllProducts();

        assertEquals(1, responses.size());
        assertEquals("Product 1", responses.get(0).getName());
    }

    @Test
    void deleteProduct_shouldEvictCache() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(redisTemplate).delete("product:1");
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_shouldThrowWhenNotFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> productService.deleteProduct(99L));
    }
}
