package net.virtualboss.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DateType {
    TARGET_START(1),
    TARGET_FINISH(2),
    ACTUAL_FINISH(3),
    ANY_DATE_FIELD(4);

    private final int value;
}
