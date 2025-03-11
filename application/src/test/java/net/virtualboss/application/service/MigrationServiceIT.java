package net.virtualboss.application.service;

import net.virtualboss.common.model.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MigrationServiceIT extends BaseIntegrationTest {

    @Value("${migration.test-data-path}")
    private String testDataPath;

    @Test
    void testMigrateEmployees() {
        migrationService.migrate(testDataPath);

        List<Employee> employees = jdbcTemplate.query(
                "SELECT * FROM employees",
                (rs, rowNum) -> Employee.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .name(rs.getString("name"))
                        .build()
        );
        assertEquals(10, employees.size());
    }
}