package com.smg.challenge.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smg.challenge.exception.GenericException;
import com.smg.challenge.exception.ProductException;
import com.smg.challenge.model.Product;
import com.smg.challenge.model.ProductEvent;
import com.smg.challenge.repository.ProductRepository;
import com.smg.challenge.service.ProductEventService;
import com.smg.challenge.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private static final String AGGREGATE_TYPE_PRODUCT = "Product";
    private static final String EVENT_TYPE_PRODUCT_CREATED = "ProductCreated";

    private final ProductRepository productRepository;
    private final ProductEventService productEventService;
    private final ObjectMapper objectMapper;


    public ProductServiceImpl(ProductRepository productRepository,
                              ProductEventService productEventService,
                              ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.productEventService = productEventService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product == null) {
            throw new ProductException("Product is invalid");
        }
        log.info("Creating product: {}", product);
        product.setCreatedAt(Instant.now());
        Product savedProduct = productRepository.save(product);
        log.debug("Saved product: {}", savedProduct);
        String productPayload;
        try {
            productPayload = objectMapper.writeValueAsString(savedProduct);
            log.debug("Serialized product to JSON: {}", productPayload);
        } catch (Exception e) {
            throw new GenericException("Serialization failed", e);
        }
        createAndSaveProductCreatedEvent(savedProduct, productPayload);
        log.info("Product created successfully with ID {}", savedProduct.getId());
        return savedProduct;
    }

    private void createAndSaveProductCreatedEvent(Product savedProduct, String productPayload) {
        log.debug("Creating ProductCreated event for productId={}", savedProduct.getId());
        ProductEvent event = ProductEvent.builder()
                .aggregateType(AGGREGATE_TYPE_PRODUCT)
                .aggregateId(savedProduct.getId())
                .eventType(EVENT_TYPE_PRODUCT_CREATED)
                .payload(productPayload)
                .createdAt(Instant.now())
                .published(false)
                .build();
        productEventService.save(event);
        log.info("ProductEvent saved for productId={}", savedProduct.getId());
    }

}

