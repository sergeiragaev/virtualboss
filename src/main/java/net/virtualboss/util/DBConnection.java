package net.virtualboss.util;

import lombok.extern.log4j.Log4j2;
import net.virtualboss.config.DbConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Log4j2
public class DBConnection {

    private static Connection connection;

    public static StringBuilder multiInsert = new StringBuilder();

    private DBConnection() {
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        DbConfig.getDbUrl() + "?user="
                                + DbConfig.getDbUser()
                                + "&password=" + DbConfig.getDbPass());
            } catch (SQLException e) {
                log.info("There is error while connect to DB: {}", e.getLocalizedMessage());
            }
        }
        return connection;
    }

    public static void executeMultiInsert(String table, List<String> fields) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(table).append("(");
        for (int i = 0; i < fields.size(); i++) {
            sql.append((i > 0 ? "," : "")).append(fields.get(i));
        }
        sql.append(") VALUES ");
        sql.append(multiInsert.toString());
        try (Statement statement = DBConnection.getConnection().createStatement()) {
            statement.execute(sql.toString());
            multiInsert = new StringBuilder();
        } catch (Exception e) {
            log.info("Error occurred while inserting data: {}", e.getLocalizedMessage());
        }
    }

    public static void addRow(List<Object> values) {
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

    public static void updateTasksNumberSequence(Integer maxNumber) throws SQLException {
        try (Statement statement = DBConnection.getConnection().createStatement()) {
            statement.execute("ALTER SEQUENCE tasks_number_seq restart with " + maxNumber + " ;");
        } catch (Exception e) {
            log.info("Error occurred while updating sequence of tasks table: {}", e.getLocalizedMessage());
        }
    }

}
