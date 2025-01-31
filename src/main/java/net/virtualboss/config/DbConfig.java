package net.virtualboss.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class DbConfig {

    private String dbUrl;
    private String dbUser;
    private String dbPass;

    @Autowired
    public void loadDbConfig(
            @Value("${spring.datasource.url}") String dbUrl,
            @Value("${spring.datasource.username}") String user,
            @Value("${spring.datasource.password}") String password) {

        this.dbUrl = dbUrl;
        this.dbUser = user;
        this.dbPass = password;
    }
}
