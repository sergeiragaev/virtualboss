package net.virtualboss.migration.processor.job;

import com.linuxense.javadbf.DBFRow;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.migration.processor.BaseEntityProcessor;
import net.virtualboss.migration.service.DatabaseSaver;
import net.virtualboss.migration.processor.EntityCache;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
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

        values.put("owner", values.get("owner_id"));
        UUID contactId = databaseSaver.addJobContact(values);
        values.remove("owner");
        values.put("owner_id", contactId);

        values.remove("company");

        values.remove("email");

        values.remove("homePhone");
        values.remove("workPhone");
        values.remove("cellPhone");
        values.remove("fax");

        values.remove("address1");
        values.remove("address2");
        values.remove("city");
        values.remove("state");
        values.remove("postal");
        values.remove("country");

        databaseSaver.saveToDatabase(config.getName(), values);

    }
}