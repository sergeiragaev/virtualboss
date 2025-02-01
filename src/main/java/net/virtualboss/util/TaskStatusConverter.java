package net.virtualboss.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import net.virtualboss.model.enums.TaskStatus;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = true)
public class TaskStatusConverter implements AttributeConverter<TaskStatus, String> {

    @Override
    public String convertToDatabaseColumn(TaskStatus status) {
        if (status == null) {
            return null;
        }
        String name = status.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    @Override
    public TaskStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return TaskStatus.valueOf(dbValue.toUpperCase());
    }
}