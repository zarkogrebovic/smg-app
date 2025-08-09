package com.smg.challenge.integration;

import com.smg.challenge.model.Product;
import com.smg.challenge.model.ProductEvent;
import com.smg.challenge.repository.ProductEventRepository;
import com.smg.challenge.repository.ProductRepository;
import com.smg.challenge.service.ProductEventService;
import com.smg.challenge.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"scheduling.enabled: true"})
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgresContainer;

    @Container
    static KafkaContainer kafkaContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass");
        postgresContainer.start();

        kafkaContainer = new KafkaContainer(
                DockerImageName.parse("confluentinc/cp-kafka:7.5.1")
        );
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL config
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        // Kafka config
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductEventService productEventService;

    @Autowired
    private ProductEventRepository productEventRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void createProduct_savesToDbAndSendsKafka() {
        Product product = new Product();
        product.setName("IntegrationTestProduct");
        product.setPrice(BigDecimal.valueOf(123.45));

        Product saved = productService.createProduct(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findById(saved.getId())).isPresent();

        List<ProductEvent> productEvent = productEventService.findTopUnpublishedOrderByCreatedAt(1);

        kafkaTemplate.send("products", productEvent.getFirst().getPayload());
        productEventService.markPublished(productEvent.getFirst().getId());
        ProductEvent event = productEventRepository.findById(productEvent.getFirst().getId()).orElseThrow();
        assertThat(event.getAggregateId()).isEqualTo(saved.getId());
    }

}
