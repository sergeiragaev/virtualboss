package net.virtualboss.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DateCriteria {
    ON_OR_BEFORE(1),
    ON_OR_AFTER(2),
    EXACT(3);

    private final int value;
}
