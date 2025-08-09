package com.smg.challenge.service.impl;

import com.smg.challenge.model.ProductEvent;
import com.smg.challenge.repository.ProductEventRepository;
import com.smg.challenge.service.ProductEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProductEventServiceImpl implements ProductEventService {

    private final ProductEventRepository productEventRepository;

    public ProductEventServiceImpl(ProductEventRepository productEventRepository) {
        this.productEventRepository = productEventRepository;
    }

    @Override
    @Transactional
    public ProductEvent save(ProductEvent event) {
        return productEventRepository.save(event);
    }

    @Override
    public List<ProductEvent> findTopUnpublishedOrderByCreatedAt(int limit) {
        return productEventRepository.findTopUnpublishedOrderByCreatedAt(PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    public void markPublished(UUID eventId) {
        ProductEvent event = productEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setPublished(true);
        log.debug("Marking event with ID {} as published", eventId);
        productEventRepository.save(event);
    }

}
