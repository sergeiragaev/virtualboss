package net.virtualboss.migration.processor;

import com.linuxense.javadbf.DBFRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.virtualboss.common.exception.DataMigrationException;
import net.virtualboss.common.model.enums.EntityType;
import net.virtualboss.migration.config.MigrationConfig;
import net.virtualboss.migration.service.DatabaseSaver;
import org.springframework.security.crypto.password.PasswordEncoder;

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
                } catch (Exception e) {
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

        if (config.getIdField() != null) {
            cashes.get(config.getName() + "Cache")
                    .add(values.get(config.getIdField()), UUID.fromString(values.get("id").toString()));
            values.remove(config.getIdField());
        }

        return values;
    }

    private String generateValue() {
        return UUID.randomUUID().toString();
    }

    private Object processColumn(DBFRow row, MigrationConfig.ColumnMapping column) {
        Object rawValue;
        if (column.isAssigned()) {
            rawValue = column.getSource();
        } else {
            rawValue = row.getObject(column.getSource());
        }

        if (column.getReference() != null) {
            EntityCache cache = cashes.get(column.getReference() + "Cache");
            rawValue = cache.get(rawValue.toString());
        }

        return switch (column.getType().toUpperCase()) {
            case "STRING" -> processString(rawValue, column.getProcessor());
            case "INTEGER" -> Integer.parseInt(rawValue.toString());
            case "BOOLEAN" -> Boolean.getBoolean(rawValue == null ? "FALSE" : rawValue.toString());
            default -> rawValue;
        };
    }

    private String processString(Object value, String processor) {
        if (processor == null) return value.toString();
        return switch (processor) {
            case "sanitizeMemo" -> sanitizeMemo(value.toString());
            case "hashPassword" -> hashPassword(value.toString());
            case "assignGroupType" -> assignGroupType(value.toString());
            case "convertColor" -> convertColor(value.toString());
            default -> value.toString();
        };
    }

    private String assignGroupType(String input) {
        return switch (input.toUpperCase()) {
            case "T" -> EntityType.TASK.toString();
            case "J" -> EntityType.JOB.toString();
            case "C" -> EntityType.CONTACT.toString();
            default -> EntityType.EMPLOYEE.toString();
        };
    }

    private String hashPassword(String input) {
        return input == null ? null : encoder.encode(input);
    }

    private String sanitizeMemo(String input) {
        return input.replace("\u0000", "").trim();
    }

    private String convertColor(String cColor) {
        try {
            int color = Integer.parseInt(cColor);
            int red = getRed(color);
            int green = getGreen(color);
            int blue = getBlue(color);

            String redHex = String.format("%02X", red);
            String greenHex = String.format("%02X", green);
            String blueHex = String.format("%02X", blue);

            return "#" + redHex + greenHex + blueHex;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid color value: " + cColor, e);
        }
    }

    private static int getRed(int color) {
        return (color & 0xFF);
    }

    private static int getGreen(int color) {
        return (color >> 8 & 0xFF);
    }

    private static int getBlue(int color) {
        return (color >> 16 & 0xFF);
    }
}
