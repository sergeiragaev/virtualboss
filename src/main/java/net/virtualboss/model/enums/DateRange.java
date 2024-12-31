package net.virtualboss.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DateRange {
    TODAY(1),
    DATE_PERIOD(4),
    EXACT_DATE(5);
    private final int value;
}
