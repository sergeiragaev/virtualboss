package net.virtualboss.application.service;

import net.virtualboss.common.model.entity.Employee;
import net.virtualboss.common.model.entity.Task;
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
    void testMigrateData() {
        migrationService.migrate(testDataPath);

        List<Employee> employees = jdbcTemplate.query(
                "SELECT * FROM employees",
                (rs, rowNum) -> Employee.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .name(rs.getString("name"))
                        .build()
        );
        assertEquals(3, employees.size());
        List<Task> tasks = jdbcTemplate.query(
                "SELECT * FROM tasks",
                (rs, rowNum) -> Task.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .description(rs.getString("description"))
                        .build()
        );
        assertEquals(180, tasks.size());
    }
}