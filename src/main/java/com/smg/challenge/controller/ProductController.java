package com.smg.challenge.controller;

import com.smg.challenge.dto.ProductRequest;
import com.smg.challenge.dto.ProductResponse;
import com.smg.challenge.mapper.ProductMapper;
import com.smg.challenge.model.Product;
import com.smg.challenge.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/product")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @PostMapping
    @Operation(
            summary = "Create Product",
            description = "Creates a new product",
            tags = {"Product"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product create request",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "ProductRequest Example",
                                    summary = "A valid request example",
                                    value = "{\n  \"name\": \"Test Product\",\n  \"price\": 18.99\n}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created product",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json",
                                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                                            name = "ProductResponse Example",
                                            summary = "A valid response example",
                                            value = "{\n  \"id\": \"2d535b60-2bc0-490b-9ab0-46499c333cc1\",\n  \"name\": \"Test Product\",\n  \"price\": 19.99,\n  \"createdAt\": \"2024-08-09T00:00:00Z\"\n}"
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid request data"),
                    @ApiResponse(responseCode = "500", description = "Server error")
            }
    )

    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        Product product = productMapper.toProduct(request);
        log.info("Creating product: {}", product);
        Product created = productService.createProduct(product);
        ProductResponse response = productMapper.toProductResponse(created);
        log.info("Created product: {}", response);
        return ResponseEntity.ok(response);
    }
}
