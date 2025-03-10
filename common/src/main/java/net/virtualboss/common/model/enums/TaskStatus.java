package net.virtualboss.common.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum TaskStatus {
    ACTIVE,
    DONE;

    @JsonValue
    public String toCamelCase() {
        String name = this.name();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    @JsonCreator
    public static TaskStatus fromCamelCase(String value) {
        return Arrays.stream(values())
                .filter(status -> status.toCamelCase().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid status: " + value));
    }
}