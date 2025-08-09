package com.smg.challenge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEvent {
    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Aggregate type is required")
    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType; // e.g. "Product"

    @NotNull(message = "Aggregate ID is required")
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @NotBlank(message = "Event type is required")
    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType; // e.g. "ProductCreated"

    @NotBlank(message = "Payload is required")
    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "published", nullable = false)
    private boolean published = false;

    @PastOrPresent(message = "Created date cannot be in the future")
    @NotNull(message = "Created date is required")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}