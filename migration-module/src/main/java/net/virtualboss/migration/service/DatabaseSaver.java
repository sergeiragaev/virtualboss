package net.virtualboss.migration.service;

import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.MigrationException;
import net.virtualboss.common.model.entity.Field;
import net.virtualboss.common.web.dto.CustomValueDto;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.migration.dto.FieldRowMapper;
import net.virtualboss.migration.dto.FieldValueRowMapper;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class DatabaseSaver {

    private final JdbcTemplate jdbcTemplate;
    private final MigrationConfig migrationConfig;
    private final Map<String, List<Map<String, Object>>> batchBuffer = new HashMap<>();
    private final Map<String, String> entityToTableMap = new HashMap<>();

    private final Map<String, List<Pair<String, String>>> customFieldBuffer = new HashMap<>();
    private final Map<String, Field> fieldCache = new ConcurrentHashMap<>();
    private final Map<String, Long> fieldValueCache = new ConcurrentHashMap<>();

    private final Map<String, List<Map<String, Object>>> attachmentsBuffer = new HashMap<>();
    private final Map<String, Long> resourceCache = new ConcurrentHashMap<>();

    private final Map<Pair<String, String>, UUID> communicationTypeCache = new ConcurrentHashMap<>();
    private final Map<String, List<Map<String, Object>>> communicationsBuffer = new HashMap<>();

    private final Map<String, UUID> contactCache = new ConcurrentHashMap<>();

    private final Map<String, List<Map<String, Object>>> addressesBuffer = new HashMap<>();

    private final Map<String, UUID> companyCache = new ConcurrentHashMap<>();

    private static final String FLUSHING_BUFFER_MESSAGE = "Flushing buffer for {} ({} records)";

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

        log.info(FLUSHING_BUFFER_MESSAGE, entityName, buffer.size());

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> row = buffer.get(i);
                int index = 1;
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    setValue(ps, index++, entry.getValue(), config.getColumns()
                            .stream().filter(columnMapping ->
                                    columnMapping.getName().equals(entry.getKey())
                            ).findFirst().orElseThrow(() -> new MigrationException(entry.getKey()))
                            .getType());
                }
            }

            @Override
            public int getBatchSize() {
                return buffer.size();
            }
        });

        log.info("Successfully inserted {} records into {}", buffer.size(), tableName);

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
        flushCustomFields();
        flushAttachments();
        flushCommunications();
        flushAddresses();
    }

    @Transactional
    public void addCustomField(String entityId, String fieldName, String value) {
        if (value.isBlank()) return;

        customFieldBuffer
                .computeIfAbsent(entityId, k -> new ArrayList<>())
                .add(Pair.of(fieldName, value));

        if (customFieldBuffer.size() >= migrationConfig.getBatchSize()) {
            flushCustomFields();
        }
    }

    public void flushCustomFields() {
        if (customFieldBuffer.isEmpty()) return;

        List<Object[]> batchArgs = new ArrayList<>();

        customFieldBuffer.forEach((entityId, fields) ->
                fields.forEach(
                        pair -> {
                            String fieldName = pair.getFirst();
                            String value = pair.getSecond();

                            Field field = getOrCacheField(fieldName);
                            Long fieldValueId = getOrCacheFieldValue(field, value);

                            batchArgs.add(new Object[]{entityId, fieldValueId});
                        }
                )
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO entity_custom_values (entity_id, custom_value_id) VALUES (?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] args = batchArgs.get(i);
                        ps.setObject(1, UUID.fromString(args[0].toString()), Types.OTHER);
                        ps.setLong(2, (Long) args[1]);
                    }

                    @Override
                    public int getBatchSize() {
                        return batchArgs.size();
                    }
                }
        );

        log.info(FLUSHING_BUFFER_MESSAGE, "Custom fields", customFieldBuffer.size());

        customFieldBuffer.clear();
    }

    private Field getOrCacheField(String fieldName) {
        return fieldCache.computeIfAbsent(fieldName, name ->
                jdbcTemplate.queryForObject(
                        "SELECT * FROM fields WHERE name = ?",
                        new FieldRowMapper(),
                        name
                )
        );
    }

    private Long getOrCacheFieldValue(Field field, String value) {
        String cacheKey = field.getId() + ":" + value;
        return fieldValueCache.computeIfAbsent(cacheKey, key -> {

            List<Long> existingIds = jdbcTemplate.query(connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                "SELECT id FROM custom_values WHERE field_id = ? AND custom_value = ?"
                        );
                        ps.setLong(1, field.getId());
                        ps.setString(2, value);
                        return ps;
                    },
                    (rs, rowNum) -> rs.getLong("id")
            );

            if (!existingIds.isEmpty()) {
                return existingIds.get(0);
            }

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO custom_values (field_id, custom_value) VALUES (?, ?)",
                        new String[]{"id"}
                );
                ps.setLong(1, field.getId());
                ps.setString(2, value);
                return ps;
            }, keyHolder);

            log.info("New value inserted: {}.{} = {}", field.getName(), key, value);

            return Objects.requireNonNull(keyHolder.getKey()).longValue();
        });
    }

    public void preloadCaches() {
        List<Field> fields = jdbcTemplate.query(
                "SELECT * FROM fields",
                new FieldRowMapper()
        );
        fields.forEach(f -> fieldCache.put(f.getName(), f));

        List<CustomValueDto> values = jdbcTemplate.query(
                "SELECT * FROM custom_values",
                new FieldValueRowMapper()
        );
        values.forEach(v -> fieldValueCache.put(
                v.getFieldId() + ":" + v.getFieldValue(),
                v.getId()
        ));
    }

    @Transactional
    public void addAttachments(String entityId, String fullPath, String uncPath, Boolean clip) {
        if (fullPath.isBlank()) return;
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("fullPath", fullPath);
        attachment.put("uncPath", uncPath);
        attachment.put("clip", clip);

        attachmentsBuffer
                .computeIfAbsent(entityId, k -> new ArrayList<>())
                .add(attachment);

        if (attachmentsBuffer.size() >= migrationConfig.getBatchSize()) {
            flushAttachments();
        }
    }

    private Long getOrCacheResourceId(String fullPath, String uncPath) {
        return resourceCache.computeIfAbsent(fullPath, path -> {
                    List<Long> existingIds = jdbcTemplate.query(connection -> {
                                PreparedStatement ps = connection.prepareStatement(
                                        "SELECT id FROM resources WHERE lower(all_full_path) = ?"
                                );
                                ps.setString(1, fullPath.toLowerCase());
                                return ps;
                            },
                            (rs, rowNum) -> rs.getLong("id")
                    );
                    if (!existingIds.isEmpty()) {
                        return existingIds.get(0);
                    }

                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO resources (all_full_path, unc_full_path) VALUES (?, ?)",
                                new String[]{"id"}
                        );
                        ps.setString(1, fullPath);
                        ps.setString(2, uncPath);
                        return ps;
                    }, keyHolder);

                    log.info("New resource inserted: {}", fullPath);

                    return Objects.requireNonNull(keyHolder.getKey()).longValue();
                }
        );
    }

    public void flushAttachments() {
        if (attachmentsBuffer.isEmpty()) return;

        List<Object[]> batchArgs = new ArrayList<>();

        attachmentsBuffer.forEach((entityId, paths) ->
                paths.forEach(
                        path -> {
                            String fullPath = path.get("fullPath").toString();
                            String uncPath = path.get("uncPath").toString();
                            Boolean clip = (Boolean) path.get("clip");

                            Long resourceId = getOrCacheResourceId(fullPath, uncPath);

                            batchArgs.add(new Object[]{entityId, resourceId, clip});
                        }
                )
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO task_attachments (task_id, resource_id, is_clip) VALUES (?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] args = batchArgs.get(i);
                        ps.setObject(1, UUID.fromString(args[0].toString()), Types.OTHER);
                        ps.setLong(2, (Long) args[1]);
                        ps.setBoolean(3, Boolean.parseBoolean(args[2].toString()));
                    }

                    @Override
                    public int getBatchSize() {
                        return batchArgs.size();
                    }
                }
        );

        log.info(FLUSHING_BUFFER_MESSAGE, "Attachments", attachmentsBuffer.size());

        attachmentsBuffer.clear();
    }

    @Transactional
    public void addCommunications(String entityId, String caption, String channel, String title) {
        if (title.isBlank()) return;
        Map<String, Object> communication = new HashMap<>();
        communication.put("caption", caption);
        communication.put("channel", channel);
        communication.put("title", title);

        communicationsBuffer
                .computeIfAbsent(entityId, k -> new ArrayList<>())
                .add(communication);

        if (communicationsBuffer.size() >= migrationConfig.getBatchSize()) {
            flushCommunications();
        }
    }

    private UUID getOrCacheCommunicationId(String caption, String channel) {
        return communicationTypeCache.computeIfAbsent(Pair.of(caption, channel), pair -> {
                    List<UUID> existingIds = jdbcTemplate.query(connection -> {
                                PreparedStatement ps = connection.prepareStatement(
                                        "SELECT id FROM communication_types WHERE lower(caption) = ? and channel = ?"
                                );
                                ps.setString(1, pair.getFirst().toLowerCase());
                                ps.setString(2, pair.getSecond());
                                return ps;
                            },
                            (rs, rowNum) -> UUID.fromString(rs.getObject("id").toString())
                    );
                    if (!existingIds.isEmpty()) {
                        return existingIds.get(0);
                    }

                    UUID id = UUID.randomUUID();
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO communication_types (id, caption, channel, is_deleted) VALUES (?, ?, ?, ?)"
                        );
                        ps.setObject(1, id, Types.OTHER);
                        ps.setString(2, pair.getFirst());
                        ps.setString(3, pair.getSecond());
                        ps.setBoolean(4, false);

                        return ps;
                    });

                    log.info("New communication type {} with caption {} inserted",
                            pair.getFirst(), pair.getSecond());

                    return id;
                }
        );
    }

    public void flushCommunications() {
        if (communicationsBuffer.isEmpty()) return;

        List<Object[]> batchArgs = new ArrayList<>();

        communicationsBuffer.forEach((entityId, communications) ->
                communications.forEach(
                        communication -> {
                            String title = communication.get("title").toString();
                            String caption = communication.get("caption").toString();
                            String channel = communication.get("channel").toString();

                            UUID communicationId = getOrCacheCommunicationId(caption, channel);
                            UUID id = UUID.randomUUID();

                            batchArgs.add(new Object[]{id, entityId, communicationId, title});
                        }
                )
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO communications (id, entity_id, type_id, title, is_deleted) VALUES (?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] args = batchArgs.get(i);
                        ps.setObject(1, UUID.fromString(args[0].toString()), Types.OTHER);
                        ps.setObject(2, UUID.fromString(args[1].toString()), Types.OTHER);
                        ps.setObject(3, UUID.fromString(args[2].toString()), Types.OTHER);
                        ps.setString(4, args[3].toString());
                        ps.setBoolean(5, false);
                    }

                    @Override
                    public int getBatchSize() {
                        return batchArgs.size();
                    }
                }
        );

        log.info(FLUSHING_BUFFER_MESSAGE, "Communications", communicationsBuffer.size());

        communicationsBuffer.clear();
    }

    public UUID addJobContact(Map<String, Object> values) {

        String name = values.get("owner").toString();
        String company = values.get("company").toString();

        UUID contactId = getOrCacheContactId(name, company);

        addJobAddress(contactId.toString(), values);

        addCommunications(
                contactId.toString(), "Job Home", "PHONE",
                values.get("homePhone").toString()
        );
        addCommunications(
                contactId.toString(), "Job Work", "PHONE",
                values.get("workPhone").toString()
        );
        addCommunications(
                contactId.toString(), "Job Cellular", "PHONE",
                values.get("cellPhone").toString()
        );
        addCommunications(
                contactId.toString(), "Job Fax", "PHONE",
                values.get("fax").toString()
        );

        return contactId;
    }

    private void addJobAddress(String contactId, Map<String, Object> values) {
        Map<String, Object> address = new HashMap<>();
        address.put("address1", values.get("address1"));
        address.put("address2", values.get("address2"));
        address.put("city", values.get("city"));
        address.put("state", values.get("state"));
        address.put("postal", values.get("postal"));
        address.put("country", values.get("country"));

        addressesBuffer
                .computeIfAbsent(contactId, k -> new ArrayList<>())
                .add(address);

        if (addressesBuffer.size() >= migrationConfig.getBatchSize()) {
            flushAddresses();
        }
    }

    private void flushAddresses() {
        if (addressesBuffer.isEmpty()) return;

        List<Object[]> batchArgs = new ArrayList<>();

        addressesBuffer.forEach((entityId, addresses) ->
                addresses.forEach(
                        address -> {
                            String address1 = address.get("address1").toString();
                            String address2 = address.get("address2").toString();
                            String city = address.get("city").toString();
                            String state = address.get("state").toString();
                            String postal = address.get("postal").toString();
                            String country = address.get("country").toString();

                            UUID id = UUID.randomUUID();

                            UUID communicationId = getOrCacheCommunicationId("Job site", "ADDRESS");

                            batchArgs.add(new Object[]{id, entityId, communicationId, address1, address2,
                                    city, state, postal, country});
                        }
                )
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO addresses (" +
                        "id, entity_id, type_id, address1, address2, city, state, postal, country, is_deleted" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] args = batchArgs.get(i);
                        ps.setObject(1, UUID.fromString(args[0].toString()), Types.OTHER);
                        ps.setObject(2, UUID.fromString(args[1].toString()), Types.OTHER);
                        ps.setObject(3, UUID.fromString(args[2].toString()), Types.OTHER);
                        ps.setString(4, args[3].toString());
                        ps.setString(5, args[4].toString());
                        ps.setString(6, args[5].toString());
                        ps.setString(7, args[6].toString());
                        ps.setString(8, args[7].toString());
                        ps.setString(9, args[8].toString());
                        ps.setBoolean(10, false);
                    }

                    @Override
                    public int getBatchSize() {
                        return batchArgs.size();
                    }
                }
        );

        log.info(FLUSHING_BUFFER_MESSAGE, "Job addresses", addressesBuffer.size());

        addressesBuffer.clear();
    }

    private UUID getOrCacheContactId(String name, String company) {
        String firstName;
        String lastName;
        String[] parts = name.split(" ");

        if (parts.length > 2) {
            lastName = parts[parts.length - 1];
            firstName = name.replace(lastName, "").trim();
        } else if (parts.length > 1) {
            firstName = parts[0];
            lastName = parts[1];
        } else {
            lastName = "";
            firstName = name;
        }

        UUID companyId = getOrCacheCompanyId(company);

        return contactCache.computeIfAbsent(name, contact -> {
                    List<UUID> existingIds = jdbcTemplate.query(connection -> {
                                PreparedStatement ps = connection.prepareStatement(
                                        "SELECT id FROM contacts WHERE lower(first_name) = ? and lower(last_name) = ?"
                                );
                                ps.setString(1, firstName.toLowerCase());
                                ps.setString(2, lastName.toLowerCase());
                                return ps;
                            },
                            (rs, rowNum) -> UUID.fromString(rs.getObject("id").toString())
                    );
                    if (!existingIds.isEmpty()) {
                        return existingIds.get(0);
                    }

                    UUID id = UUID.randomUUID();
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO contacts (id, first_name, last_name, company_id, is_deleted) " +
                                        "VALUES (?, ?, ?, ?, ?)"
                        );
                        ps.setObject(1, id, Types.OTHER);
                        ps.setString(2, firstName);
                        ps.setString(3, lastName);
                        ps.setObject(4, companyId, Types.OTHER);
                        ps.setBoolean(5, false);

                        return ps;
                    });

                    log.info("New contact {} with first name {} and lastname {} added",
                            name, firstName, lastName);

                    return id;
                }
        );
    }

    private UUID getOrCacheCompanyId(String name) {

        if (name.isBlank()) return null;

        return companyCache.computeIfAbsent(name, company -> {
                    List<UUID> existingIds = jdbcTemplate.query(connection -> {
                                PreparedStatement ps = connection.prepareStatement(
                                        "SELECT id FROM companies WHERE lower(name) = ?"
                                );
                                ps.setString(1, name.toLowerCase());
                                return ps;
                            },
                            (rs, rowNum) -> UUID.fromString(rs.getObject("id").toString())
                    );
                    if (!existingIds.isEmpty()) {
                        return existingIds.get(0);
                    }

                    UUID id = UUID.randomUUID();
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO companies (id, name, is_deleted) VALUES (?, ?, ?)"
                        );
                        ps.setObject(1, id, Types.OTHER);
                        ps.setString(2, name);
                        ps.setBoolean(3, false);

                        return ps;
                    });

                    log.info("New company {} added", name);

                    return id;
                }
        );
    }
}