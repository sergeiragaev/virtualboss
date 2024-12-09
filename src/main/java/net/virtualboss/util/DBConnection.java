package net.virtualboss.util;

import net.virtualboss.config.DbConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class DBConnection {

    private static Connection connection;

    public static StringBuilder multiInsert = new StringBuilder();

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(
                        DbConfig.getDbUrl() + "?user="
                                + DbConfig.getDbUser()
                                + "&password=" + DbConfig.getDbPass());
            } catch (SQLException e) {
                e.printStackTrace();
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
        DBConnection.getConnection().createStatement().execute(sql.toString());
        multiInsert = new StringBuilder();
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
}
