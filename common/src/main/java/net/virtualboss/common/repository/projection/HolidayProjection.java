package net.virtualboss.common.repository.projection;

import java.time.LocalDate;

public interface HolidayProjection {
    LocalDate getDate();  // Field "date" from the holidays table
    String getName();     // Field "name" from the holidays table
}