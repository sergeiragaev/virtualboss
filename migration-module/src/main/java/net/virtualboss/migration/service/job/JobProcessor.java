package net.virtualboss.migration.service.job;

import com.linuxense.javadbf.DBFRow;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.migration.service.BaseEntityProcessor;
import net.virtualboss.migration.service.DatabaseSaver;
import net.virtualboss.migration.service.EntityCache;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class JobProcessor extends BaseEntityProcessor {

    public JobProcessor(
            PasswordEncoder encoder,
            MigrationConfig migrationConfig,
            DatabaseSaver databaseSaver,
            Map<String, EntityCache> cashes) {
        super(encoder, migrationConfig, databaseSaver, cashes);
    }

    @Override
    public void process(DBFRow row) {
        MigrationConfig.EntityConfig config = migrationConfig.getEntities().get("job");

        Map<String, Object> values = process(row, config);

        cashes.get("jobCache").add(values.get("number").toString(), UUID.fromString(values.get("id").toString()));

        databaseSaver.saveToDatabase(config.getName(), values);

    }
}