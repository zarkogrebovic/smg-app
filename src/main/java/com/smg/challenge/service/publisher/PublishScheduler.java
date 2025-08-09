package com.smg.challenge.service.publisher;

import com.smg.challenge.model.ProductEvent;
import com.smg.challenge.repository.ProductEventRepository;
import com.smg.challenge.service.ProductEventService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
@ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class PublishScheduler {

    private final ProductEventService productEventService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public PublishScheduler(ProductEventService productEventService, KafkaTemplate<String, String> kafkaTemplate) {
        this.productEventService = productEventService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void init() {
        log.info("PublishScheduler is active");
    }

    @Scheduled(fixedDelay = 1500)
    public void publishEvents() {
        log.debug("Querying unpublished product events for scheduled publishing");
        List<ProductEvent> events = productEventService.findTopUnpublishedOrderByCreatedAt(20);
        log.debug("Found {} unpublished product events to process", events.size());

        for (ProductEvent event : events) {
            log.debug("Publishing event with ID {} to Kafka", event.getId());
            kafkaTemplate.send("products", event.getPayload())
                    .thenAccept(result -> {
                        productEventService.markPublished(event.getId());
                        log.debug("Successfully published and persisted event ID {} to Kafka", event.getId());
                    })
                    .exceptionally(ex -> {
                        log.error("Kafka send failed for event ID {}: {}", event.getId(), ex.getMessage(), ex);
                        return null;
                    });
        }
    }
}
