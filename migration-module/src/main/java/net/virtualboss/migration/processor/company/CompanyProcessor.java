package net.virtualboss.migration.processor.company;

import com.linuxense.javadbf.DBFRow;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.migration.processor.BaseEntityProcessor;
import net.virtualboss.migration.processor.EntityCache;
import net.virtualboss.migration.service.DatabaseSaver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CompanyProcessor extends BaseEntityProcessor {

    public CompanyProcessor(
            PasswordEncoder encoder,
            MigrationConfig migrationConfig,
            DatabaseSaver databaseSaver,
            Map<String, EntityCache> cashes) {
        super(encoder, migrationConfig, databaseSaver, cashes);
    }

    @Override
    public void process(DBFRow row) {
        MigrationConfig.EntityConfig config = migrationConfig.getEntities().get("company");

        Map<String, Object> values = process(row, config);

        cashes.get("companyCache").add(values.get("name").toString(), UUID.fromString(values.get("id").toString()));

        databaseSaver.saveToDatabase(config.getName(), values);

    }
}