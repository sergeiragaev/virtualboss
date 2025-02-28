package net.virtualboss.migration.service;

import com.linuxense.javadbf.DBFRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.DataMigrationException;
import net.virtualboss.migration.config.MigrationConfig;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Log4j2
@RequiredArgsConstructor
public abstract class BaseEntityProcessor implements EntityProcessor {
    protected final PasswordEncoder encoder;
    protected final MigrationConfig migrationConfig;
    protected final DatabaseSaver databaseSaver;
    protected final Map<String, EntityCache> cashes;


    protected Map<String, Object> process(DBFRow row, MigrationConfig.EntityConfig config) {
        Map<String, Object> values = new HashMap<>();

        for (MigrationConfig.ColumnMapping column : config.getColumns()) {
            if (column.getSource() == null && !column.isGenerated()) continue;
            if (column.isGenerated()) {
                values.put(column.getName(), generateValue());
            } else {
                Object value;
                try {
                    value = processColumn(row, column);
                } catch (ParseException e) {
                    throw new DataMigrationException(
                            "Error parsing column: " + column, e);
                }
                values.put(column.getName(), value);
            }
        }

        values.put("is_deleted", false);

        if (config.getCustomFields() != null) {
            for (MigrationConfig.CustomFieldMapping customField : config.getCustomFields()) {
                String sourceValue = row.getString(customField.getSource());
                databaseSaver.addCustomField(
                        values.get("id").toString(), customField.getTarget(), sourceValue);
            }
        }

        return values;
    }

    private String generateValue() {
        return UUID.randomUUID().toString();
    }

    private Object processColumn(DBFRow row, MigrationConfig.ColumnMapping column) throws ParseException {
        Object rawValue = row.getObject(column.getSource());

        if (column.getReference() != null) {
            EntityCache cache = cashes.get(column.getReference() + "Cache");
            rawValue = cache.get(rawValue.toString());
        }

        return switch (column.getType().toUpperCase()) {
            case "STRING" -> processString(rawValue, column.getProcessor());
            case "INTEGER" -> Integer.parseInt(rawValue.toString());
            default -> rawValue;
        };
    }

    private String processString(Object value, String processor) {
        if (processor == null) return value.toString();
        return switch (processor) {
            case "sanitizeMemo" -> sanitizeMemo(value.toString());
            case "hashPassword" -> hashPassword(value.toString());
            default -> value.toString();
        };
    }

    private String hashPassword(String string) {
        return string == null ? null : encoder.encode(string);
    }

    private String sanitizeMemo(String input) {
        return input.replace("\u0000", "").trim();
    }
}
