package net.virtualboss.util;

import net.virtualboss.model.enums.TaskStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToStatusConverter implements Converter<String, TaskStatus> {
    @Override
    public TaskStatus convert(String source) {
        try {
            return TaskStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for enum: " + source);
        }
    }}
