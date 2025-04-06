package net.virtualboss.migration.service;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.field.service.FieldService;
import net.virtualboss.field.web.dto.FieldDto;
import net.virtualboss.migration.processor.EntityProcessor;
import net.virtualboss.migration.processor.relation.RelationProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class MigrationService {
    private final FieldService fieldService;
    private final DBFReaderFactory dbfReaderFactory;
    private final DatabaseSaver databaseSaver;

    private final Map<String, EntityProcessor> processors;
    private final MigrationConfig migrationConfig;

    private final RelationProcessor relationProcessor;

    @Value("${migration.test-data-path}")
    private String testDataPath;

    public void migrate(String dataPath) {

        if (dataPath == null) {
            dataPath = testDataPath;
        }



        migrateFields(dataPath);

        databaseSaver.preloadCaches();

        final String finalDataPath = dataPath;
        migrationConfig.getEntities().forEach((entityName, config) -> {
            EntityProcessor processor = processors.get(entityName + "Processor");
            processDBF(finalDataPath, config, processor);
        });

        migrationConfig.getRelations().forEach(relation -> {
            if (relation.getFrom().hasSourceFile()) {
                relationProcessor.processExternalFile(finalDataPath, relation);
            } else {
                relationProcessor.processEmbeddedField(finalDataPath, relation);
            }
        });

    }

    private void processDBF(String path, MigrationConfig.EntityConfig config, EntityProcessor processor) {
        try (DBFReader reader = dbfReaderFactory.createReader(
                path + "/" + config.getDbfFile(),
                config.getMemoFile() == null ? null : path + "/" + config.getMemoFile())) {
            DBFRow row;
            while ((row = reader.nextRow()) != null) {
                processor.process(row);
            }
            databaseSaver.flushAll();
        } catch (Exception e) {
            log.error("There is error occurred while migrate data: " + e.getLocalizedMessage(), e);
        }
    }

    private void migrateFields(String path) {
        try (DBFReader reader =
                     dbfReaderFactory.createReader(path + "/ctfields.dbf", null)) {
            updateFields(reader);
        } catch (Exception e) {
            log.error("There is error occurred while parsing ctfields.dbf: {}", e.getLocalizedMessage());
        }
    }

    private void updateFields(DBFReader reader) {
        DBFRow row;
        while ((row = reader.nextRow()) != null) {
            try {
                FieldDto fieldDto = FieldDto.builder()
                        .name(row.getString("FI_VARNAME"))
                        .defaultValue(row.getString("FI_NAME"))
                        .alias(row.getString("FI_ALIAS"))
                        .enabled(row.getBoolean("FI_RVBLIST"))
                        .fieldOrder((short) row.getInt("FI_RVBORDR"))
                        .build();
                fieldService.updateField(fieldDto);
            } catch (Exception e) {
                log.error("There is error occurred while parsing fields info: {}", e.getLocalizedMessage());
            }
        }
    }
}
