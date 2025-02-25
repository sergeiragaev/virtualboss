package net.virtualboss.migration.service.employee;

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
public class EmployeeProcessor extends BaseEntityProcessor {

    public EmployeeProcessor(PasswordEncoder encoder, MigrationConfig migrationConfig, DatabaseSaver databaseSaver, Map<String, EntityCache> cashes) {
        super(encoder, migrationConfig, databaseSaver, cashes);
    }

    @Override
    public void process(DBFRow row) {
        MigrationConfig.EntityConfig config = migrationConfig.getEntities().get("employee");

        Map<String, Object> values = process(row, config);

        cashes.get("employeeCache").add(values.get("name"), UUID.fromString(values.get("id").toString()));

        databaseSaver.saveToDatabase(config.getName(), values);

//        // Обработка кастомных полей
//        for (MigrationConfig.CustomFieldMapping customField : config.getCustomFields()) {
//            String sourceValue = row.getString(customField.getSource());
//            if (StringUtils.isNotBlank(sourceValue)) {
//                fieldValueService.saveCustomFieldValue(
//                        values.get("id"),
//                        customField.getTarget(),
//                        sourceValue
//                );
//            }
//        }

    }
}