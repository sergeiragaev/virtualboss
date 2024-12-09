package net.virtualboss.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DbConfig {
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASS;

    @Autowired
    public void loadDbConfig(
            @Value("${spring.datasource.url}") String dbUrl,
            @Value("${spring.datasource.username}") String user,
            @Value("${spring.datasource.password}") String password) {
        DB_URL = dbUrl;
        DB_USER = user;
        DB_PASS = password;
    }

    public static String getDbUrl() {
        return DB_URL;
    }

    public static String getDbUser() {
        return DB_USER;
    }

    public static String getDbPass() {
        return DB_PASS;
    }
}
