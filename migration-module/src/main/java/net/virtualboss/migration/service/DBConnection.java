package net.virtualboss.migration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.migration.config.DbConfig;
import net.virtualboss.common.repository.TaskRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.List;
import java.util.Properties;

@Log4j2
@Component
@RequiredArgsConstructor
public class DBConnection {
    private final DbConfig dbConfig;
    private final TaskRepository taskRepository;

    private StringBuilder multiInsert = new StringBuilder();

    private Connection connection;

    public Connection getConnection() {
        if (connection == null) {
            try {
                String url = dbConfig.getDbUrl();
                Properties props = new Properties();
                props.setProperty("user", dbConfig.getDbUser());
                props.setProperty("password", dbConfig.getDbPass());
                connection = DriverManager.getConnection(url, props);
            } catch (SQLException e) {
                log.info("There is error while connect to DB: {}", e.getLocalizedMessage());
            }
        }
        return connection;
    }

    public void executeMultiInsert(String table, List<String> fields) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(table).append("(");
        for (int i = 0; i < fields.size(); i++) {
            sql.append((i > 0 ? "," : "")).append(fields.get(i));
        }
        sql.append(") VALUES ");
        sql.append(multiInsert.toString());
        try (Statement statement = getConnection().createStatement()) {
            statement.execute(sql.toString());
            multiInsert = new StringBuilder();
        } catch (Exception e) {
            log.info("Error occurred while inserting data into table {}: {}", table, e.getLocalizedMessage());
        }
    }

    public void resetMultiInsert() {
        multiInsert = new StringBuilder();
    }

    @Transactional
    public void updateTasksNumberSequence() {
        taskRepository.resetTasksNumberSequenceToMaxNumber();
    }

    public void addRow(List<Object> values) {
        multiInsert.append((!multiInsert.isEmpty() ? "," : ""));
        multiInsert.append("(");
        for (int i = 0; i < values.size(); i++) {
            multiInsert.append((i > 0 ? ", " : ""));
            Object value = values.get(i);
            if (value instanceof String) {
                multiInsert.append("'").append(value).append("'");
            } else {
                multiInsert.append(value);
            }
        }
        multiInsert.append(")");
    }

    public int getMultiInsertLength() {
        return multiInsert.length();
    }
}
