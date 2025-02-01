package net.virtualboss.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DateType {
    TARGET_START(1),
    TARGET_FINISH(2),
    ACTUAL_FINISH(3),
    ANY_DATE_FIELD(4);

    private final int value;

    public static DateType fromValue(int value) {
        return Arrays.stream(values())
                .filter(dc -> dc.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DateType value: " + value));
    }
}
