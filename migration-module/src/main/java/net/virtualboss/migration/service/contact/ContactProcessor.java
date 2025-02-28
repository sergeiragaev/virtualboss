package net.virtualboss.migration.service.contact;

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
public class ContactProcessor extends BaseEntityProcessor {

    public ContactProcessor(
            PasswordEncoder encoder,
            MigrationConfig migrationConfig,
            DatabaseSaver databaseSaver,
            Map<String, EntityCache> cashes) {
        super(encoder, migrationConfig, databaseSaver, cashes);
    }

    @Override
    public void process(DBFRow row) {
        MigrationConfig.EntityConfig config = migrationConfig.getEntities().get("contact");

        Map<String, Object> values = process(row, config);

        cashes.get("contactCache").add(values.get("legacy_id").toString(), UUID.fromString(values.get("id").toString()));
        values.remove("legacy_id");

        databaseSaver.saveToDatabase(config.getName(), values);

    }
}