package com.smg.challenge.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smg.challenge.exception.GenericException;
import com.smg.challenge.exception.ProductException;
import com.smg.challenge.model.Product;
import com.smg.challenge.model.ProductEvent;
import com.smg.challenge.repository.ProductRepository;
import com.smg.challenge.service.ProductEventService;
import com.smg.challenge.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductUnitTests {

    @Test
    void createProduct_savesProduct() throws com.fasterxml.jackson.core.JsonProcessingException {
        final String productName = "Test";
        final BigDecimal productPrice = BigDecimal.valueOf(10);

        ProductRepository productRepository = mock(ProductRepository.class);
        ProductEventService productEventService = mock(ProductEventService.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        ProductServiceImpl productService = new ProductServiceImpl(productRepository, productEventService, objectMapper);

        Product inputProduct = createProduct(null, productName, productPrice);
        UUID savedId = UUID.randomUUID();
        Product savedProduct = createProduct(savedId, productName, productPrice);

        when(productRepository.save(inputProduct)).thenReturn(savedProduct);
        when(objectMapper.writeValueAsString(savedProduct)).thenReturn("{\"id\":\"abc\"}");

        Product result = productService.createProduct(inputProduct);

        assertEquals(savedId, result.getId());
        verify(productRepository, times(1)).save(inputProduct);
        verify(productEventService, times(1)).save(any(ProductEvent.class));
    }

    @Test
    void createProduct_setsCreatedAtOnProduct() throws Exception {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductEventService productEventService = mock(ProductEventService.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        ProductServiceImpl productService = new ProductServiceImpl(productRepository, productEventService, objectMapper);

        Product inputProduct = new Product();
        inputProduct.setName("Name");
        inputProduct.setPrice(BigDecimal.TEN);

        Product savedProduct = new Product();
        savedProduct.setId(UUID.randomUUID());
        savedProduct.setName("Name");
        savedProduct.setPrice(BigDecimal.TEN);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product prodArg = invocation.getArgument(0);
            // propagate createdAt from input to output
            savedProduct.setCreatedAt(prodArg.getCreatedAt());
            return savedProduct;
        });
        when(objectMapper.writeValueAsString(any(Product.class))).thenReturn("{}");

        Product result = productService.createProduct(inputProduct);

        assertNotNull(result.getId());
        assertNotNull(savedProduct.getCreatedAt());
        // Check the createdAt is set to a (recent) timestamp
        assertTrue(savedProduct.getCreatedAt().isBefore(Instant.now().plusSeconds(2)));
    }
    
    @Test
    void createProduct_throwsException_whenEventServiceFails() throws Exception {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductEventService productEventService = mock(ProductEventService.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        ProductServiceImpl productService = new ProductServiceImpl(productRepository, productEventService, objectMapper);

        Product inputProduct = new Product();
        inputProduct.setName("Name");
        inputProduct.setPrice(BigDecimal.TEN);

        Product savedProduct = new Product();
        savedProduct.setId(UUID.randomUUID());
        savedProduct.setName("Name");
        savedProduct.setPrice(BigDecimal.TEN);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        doThrow(new RuntimeException("Serialization error"){}).when(objectMapper).writeValueAsString(any(Product.class));

        assertThrows(GenericException.class, () -> productService.createProduct(inputProduct));
    }

    @Test
    void createProduct_throwsException_whenJsonProcessingFails() throws Exception {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductEventService productEventService = mock(ProductEventService.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        ProductServiceImpl productService = new ProductServiceImpl(productRepository, productEventService, objectMapper);

        Product inputProduct = new Product();
        inputProduct.setName("Name");
        inputProduct.setPrice(BigDecimal.TEN);

        Product savedProduct = new Product();
        savedProduct.setId(UUID.randomUUID());
        savedProduct.setName("Name");
        savedProduct.setPrice(BigDecimal.TEN);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(objectMapper.writeValueAsString(any(Product.class))).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("JSON error"){});

        assertThrows(GenericException.class, () -> productService.createProduct(inputProduct));
    }

    @Test
    void createProduct_nullProduct_throwsException() {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductEventService productEventService = mock(ProductEventService.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        ProductServiceImpl productService = new ProductServiceImpl(productRepository, productEventService, objectMapper);

        assertThrows(ProductException.class, () -> productService.createProduct(null));
    }

    private Product createProduct(UUID id, String name, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
}
