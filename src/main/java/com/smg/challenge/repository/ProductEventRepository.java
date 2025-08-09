package com.smg.challenge.repository;

import com.smg.challenge.model.ProductEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductEventRepository extends JpaRepository<ProductEvent, UUID> {
    List<ProductEvent> findAllByPublishedFalse();
    
    @Query("SELECT e FROM ProductEvent e WHERE e.published = false ORDER BY e.createdAt ASC")
    List<ProductEvent> findTopUnpublishedOrderByCreatedAt(Pageable pageable);
}