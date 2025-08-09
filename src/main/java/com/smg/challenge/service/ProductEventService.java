package com.smg.challenge.service;

import com.smg.challenge.model.ProductEvent;

import java.util.List;
import java.util.UUID;

public interface ProductEventService {

    List<ProductEvent> findTopUnpublishedOrderByCreatedAt(int limit);
    ProductEvent save(ProductEvent event);
    void markPublished(UUID eventId);
}
