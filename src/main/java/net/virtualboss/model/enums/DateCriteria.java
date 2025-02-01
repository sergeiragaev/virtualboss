package net.virtualboss.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DateCriteria {
    ON_OR_BEFORE(1),
    ON_OR_AFTER(2),
    EXACT(3);

    private final int value;

    public static DateCriteria fromValue(int value) {
        return Arrays.stream(values())
                .filter(dc -> dc.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid DateCriteria value: " + value));
    }
}
