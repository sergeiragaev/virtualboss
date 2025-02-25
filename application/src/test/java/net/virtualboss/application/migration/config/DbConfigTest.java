package net.virtualboss.application.migration.config;

import net.virtualboss.migration.config.DbConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbConfigTest {

    @Test
    void testDbConfig() {
        // Arrange
        String url = "jdbc:mysql://localhost:3306/mydb";
        String user = "admin";
        String password = "secret";

        // Act
        DbConfig dbConfig = new DbConfig();
        dbConfig.loadDbConfig(url, user, password);

        // Assert
        assertEquals(url, dbConfig.getDbUrl());
        assertEquals(user, dbConfig.getDbUser());
        assertEquals(password, dbConfig.getDbPass());
    }
}