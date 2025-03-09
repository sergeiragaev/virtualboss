package net.virtualboss.migration.processor.relation;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.MigrationException;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.migration.processor.BaseEntityProcessor;
import net.virtualboss.migration.processor.EntityCache;
import net.virtualboss.migration.service.DBFReaderFactory;
import net.virtualboss.migration.service.DatabaseSaver;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.*;

@Component
@Log4j2
public class RelationProcessor extends BaseEntityProcessor {

    private final DBFReaderFactory dbfReaderFactory;
    private final JdbcTemplate jdbcTemplate;

    private static final String CACHE = "Cache";


    public RelationProcessor(
            PasswordEncoder encoder,
            MigrationConfig migrationConfig,
            DatabaseSaver databaseSaver,
            Map<String, EntityCache> cashes, DBFReaderFactory dbfReaderFactory, JdbcTemplate jdbcTemplate) {
        super(encoder, migrationConfig, databaseSaver, cashes);
        this.dbfReaderFactory = dbfReaderFactory;
        this.jdbcTemplate = jdbcTemplate;
    }


    @Transactional
    public void processEmbeddedField(String path, MigrationConfig.RelationConfig relation) {
        MigrationConfig.EntityConfig fromEntity = migrationConfig.getEntities().get(relation.getFrom().getEntity());
        String sourceField = relation.getFrom().getSourceField();
        String separator = relation.getFrom().getSeparator();

        try (DBFReader reader = dbfReaderFactory.createReader(
                path + "/" + fromEntity.getDbfFile(),
                path + "/" + fromEntity.getMemoFile())) {
            DBFRow row;
            List<Object[]> batchArgs = new ArrayList<>();

            while ((row = reader.nextRow()) != null) {
                String sourceFromId = row.getString(fromEntity.getIdFieldSource());
                UUID fromUuid = cashes.get(fromEntity.getName() + CACHE).get(sourceFromId);

                String rawIds = row.getString(sourceField);

                Arrays.stream(rawIds.split(separator))
                        .map(String::trim)
                        .forEach(sourceToId -> {

                            UUID toUuid;
                            if (relation.getFrom().getType().equalsIgnoreCase("INTEGER")) {
                                toUuid = cashes.get(relation.getTo().getEntity() + CACHE).get(Integer.parseInt(sourceToId));
                            } else {
                                toUuid = cashes.get(relation.getTo().getEntity() + CACHE).get(sourceToId);
                            }

                            if (toUuid == null) return;

                            batchArgs.add(new Object[]{fromUuid, toUuid});

                            if (batchArgs.size() >= migrationConfig.getBatchSize()) {
                                executeBatchInsert(relation, batchArgs);
                                batchArgs.clear();
                            }
                        });
            }

            if (!batchArgs.isEmpty()) {
                executeBatchInsert(relation, batchArgs);
                batchArgs.clear();
            }
        } catch (Exception e) {
            throw new MigrationException(
                    MessageFormat.format("Error processing embedded relations: {0}"
                            , e.getLocalizedMessage()));
        }
    }

    @Transactional
    public void processExternalFile(String path, MigrationConfig.RelationConfig relation) {
        String sourceFile = relation.getFrom().getSourceFile();

        try (DBFReader reader = dbfReaderFactory.createReader(
                path + "/" + sourceFile, null)) {
            DBFRow row;
            List<Object[]> batchArgs = new ArrayList<>();

            while ((row = reader.nextRow()) != null) {
                Object sourceFromId = processColumn(row, relation.getFrom());
                Object sourceToId = processColumn(row, relation.getTo());

                UUID fromUuid = cashes.get(relation.getFrom().getEntity() + CACHE).get(sourceFromId);
                UUID toUuid = cashes.get(relation.getTo().getEntity() + CACHE).get(sourceToId);

                if (fromUuid == null || toUuid == null) continue;

                batchArgs.add(new Object[]{fromUuid, toUuid});

                if (batchArgs.size() >= migrationConfig.getBatchSize()) {
                    executeBatchInsert(relation, batchArgs);
                    batchArgs.clear();
                }
            }

            if (!batchArgs.isEmpty()) {
                executeBatchInsert(relation, batchArgs);
                batchArgs.clear();
            }
        } catch (Exception e) {
            throw new MigrationException(
                    MessageFormat.format("Error processing external file relations: {0}"
                            , e.getLocalizedMessage()));
        }
    }

    private void executeBatchInsert(MigrationConfig.RelationConfig relation, List<Object[]> batchArgs) {
        String fromColumn = relation.getFrom().getColumn();
        String toColumn = relation.getTo().getColumn();

        String sql = String.format(
                "INSERT INTO \"%s\" (%s, %s) VALUES (?, ?)",
                relation.getJoinTable(), fromColumn, toColumn
        );

        log.debug("SQL: {}", sql);

        log.info("Flushing buffer for {} ({} records)", relation.getJoinTable(), batchArgs.size());

        jdbcTemplate.batchUpdate(
                sql,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Object[] args = batchArgs.get(i);
                        ps.setObject(1, args[0], Types.OTHER); // fromUuid
                        ps.setObject(2, args[1], Types.OTHER); // toUuid
                    }

                    public int getBatchSize() {
                        return batchArgs.size();
                    }
                }
        );
    }

    @Override
    public void process(DBFRow row) {
        // this is empty because of no need to implement this
    }

    private Object processColumn(DBFRow row, MigrationConfig.RelationMapping column) {
        Object rawValue = row.getObject(column.getSourceField());

        return switch (column.getType().toUpperCase()) {
            case "STRING" -> rawValue.toString();
            case "INTEGER" -> Integer.parseInt(rawValue.toString());
            default -> rawValue;
        };
    }
}