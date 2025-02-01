package net.virtualboss.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DateRange {
    TODAY(1),
    DATE_PERIOD(4),
    EXACT_DATE(5);
    private final int value;

    public static DateRange fromValue(int value) {
        return Arrays.stream(values())
                .filter(dc -> dc.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DateRange value: " + value));
    }

}
