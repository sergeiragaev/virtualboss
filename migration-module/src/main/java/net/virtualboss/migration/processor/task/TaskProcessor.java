package net.virtualboss.migration.processor.task;

import com.linuxense.javadbf.DBFRow;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.migration.processor.BaseEntityProcessor;
import net.virtualboss.migration.service.DatabaseSaver;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TaskProcessor extends BaseEntityProcessor {

    public TaskProcessor(
            PasswordEncoder encoder, MigrationConfig migrationConfig, DatabaseSaver databaseSaver, Map<String, EntityCache> cashes) {
        super(encoder, migrationConfig, databaseSaver, cashes);
    }

    @Override
    public void process(DBFRow row) {
        MigrationConfig.EntityConfig config = migrationConfig.getEntities().get("task");

        Map<String, Object> values = process(row, config);

        cashes.get("taskCache").add(values.get("number"), UUID.fromString(values.get("id").toString()));

        databaseSaver.saveToDatabase(config.getName(), values);

    }
}