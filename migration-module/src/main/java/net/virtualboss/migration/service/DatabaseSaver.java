package net.virtualboss.migration.service;

import lombok.extern.log4j.Log4j2;
import net.virtualboss.migration.config.MigrationConfig;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Log4j2
@Component
public class DatabaseSaver {

    private final JdbcTemplate jdbcTemplate;
    private final MigrationConfig migrationConfig;
    private final Map<String, List<Map<String, Object>>> batchBuffer = new HashMap<>();
    private final Map<String, String> entityToTableMap = new HashMap<>();

    public DatabaseSaver(JdbcTemplate jdbcTemplate, MigrationConfig migrationConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.migrationConfig = migrationConfig;

        migrationConfig.getEntities().forEach((entityName, config) ->
                entityToTableMap.put(entityName, config.getTable()));
    }

    @Transactional
    public void saveToDatabase(String entityName, Map<String, Object> data) {
        List<Map<String, Object>> buffer = batchBuffer.computeIfAbsent(
                entityName,
                k -> new ArrayList<>()
        );

        buffer.add(data);

        if (buffer.size() >= migrationConfig.getBatchSize()) {
            flushBuffer(entityName);
        }
    }

    private void flushBuffer(String entityName) {
        List<Map<String, Object>> buffer = batchBuffer.get(entityName);
        if (buffer == null || buffer.isEmpty()) return;

        String tableName = entityToTableMap.get(entityName);
        MigrationConfig.EntityConfig config = migrationConfig.getEntities().get(entityName);

        if (tableName == null || config == null) {
            throw new IllegalStateException("Configuration for entity '" + entityName + "' not found");
        }

        String sql = buildInsertSQL(tableName, buffer.get(0).keySet());

        log.info("Flushing buffer for {} ({} records)", entityName, buffer.size());

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> row = buffer.get(i);
                int index = 1;
                for (String column : row.keySet()) {
                    setValue(ps, index++, row.get(column), config.getColumns()
                            .stream().filter(columnMapping ->
                                    columnMapping.getName().equals(column)
                            ).findFirst().orElseThrow()
                            .getType());
                }
            }

            @Override
            public int getBatchSize() {
                return buffer.size();
            }
        });

        log.debug("Successfully inserted {} records into {}", buffer.size(), tableName);

        buffer.clear();
    }

    private String buildInsertSQL(String tableName, Set<String> columns) {
        return "INSERT INTO " + tableName + " (" +
               String.join(", ", columns) + ") VALUES (" +
               String.join(", ", Collections.nCopies(columns.size(), "?")) + ")";
    }

    private void setValue(PreparedStatement ps, int index, Object value, String type)
            throws SQLException {

        if (value == null) {
            ps.setNull(index, java.sql.Types.NULL);
            return;
        }

        switch (type.toUpperCase()) {
            case "UUID":
                ps.setObject(index, value, java.sql.Types.OTHER);
                break;
            case "DATE":
                ps.setDate(index, new java.sql.Date(((java.util.Date) value).getTime()));
                break;
            case "TIMESTAMP":
                ps.setTimestamp(index, java.sql.Timestamp.valueOf(
                        LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault())));
                break;
            case "BOOLEAN":
                ps.setBoolean(index, (Boolean) value);
                break;
            case "INTEGER":
                ps.setInt(index, (Integer) value);
                break;
            case "DECIMAL":
                ps.setBigDecimal(index, (java.math.BigDecimal) value);
                break;
            default:
                ps.setString(index, value.toString());
        }
    }

    @Transactional
    public void flushAll() {
        batchBuffer.keySet().forEach(this::flushBuffer);
    }
}