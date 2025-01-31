package net.virtualboss.service.migration;

import net.virtualboss.BaseIntegrationTest;
import net.virtualboss.model.entity.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MigrationServiceIT extends BaseIntegrationTest {

    private static final String TEST_DATA_PATH = "src/test/resources/testdata";

    @Test
    void testMigrateEmployees() {
        // Запуск миграции
        migrationService.migrate(TEST_DATA_PATH);

        // Проверка данных в БД
        List<Employee> employees = jdbcTemplate.query(
                "SELECT * FROM employees",
                (rs, rowNum) -> Employee.builder()
                        .id(UUID.fromString(rs.getString("id")))
                        .name(rs.getString("name"))
                        .build()
        );
        assertEquals(1, employees.size());
    }
}