package com.smg.challenge.mapper;

import com.smg.challenge.dto.ProductRequest;
import com.smg.challenge.dto.ProductResponse;
import com.smg.challenge.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toProduct(ProductRequest request);

    ProductResponse toProductResponse(Product product);

}
