package net.virtualboss;

import net.virtualboss.config.DbConfig;
import net.virtualboss.repository.FieldRepository;
import net.virtualboss.repository.FieldValueRepository;
import net.virtualboss.service.migration.MigrationService;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    @Autowired
    protected MigrationService migrationService;

    @Autowired
    protected FieldRepository fieldRepository;

    @Autowired
    protected FieldValueRepository fieldValueRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected DbConfig dbConfig;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}