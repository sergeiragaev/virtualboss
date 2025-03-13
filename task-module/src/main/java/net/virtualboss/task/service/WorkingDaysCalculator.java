package net.virtualboss.task.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.threeten.extra.LocalDateRange;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WorkingDaysCalculator {
    private final HolidayService holidayService;

    // Method to check if a day is a weekend (Saturday or Sunday)
    private boolean isNotWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    // Method for calculating the number of working days in a date range
    public long countBusinessDays(LocalDate start, LocalDate end, String countryCode) {
        Set<LocalDate> holidays = holidayService.getHolidays(
                start.getYear(),
                countryCode
        );

        short sign = 1;
        if (start.isAfter(end)) {
            LocalDate oldStart = start;
            start = end;
            end = oldStart;
            sign = -1;
        }

        // Create a date range including the start date and excluding the end date
        LocalDateRange range = LocalDateRange.of(start, end);
        return range.stream()
                       .filter(date -> isNotWeekend(date) && !holidays.contains(date))
                       .count() * sign;
    }

    public LocalDate addWorkDays(LocalDate startDate, int days, String countryCode) {
        Set<LocalDate> holidays = holidayService.getHolidays(
                startDate.getYear(),
                countryCode
        );

        if (days < 0) {
            days = -days;
            startDate = startDate.minusDays(days);
        }

        return LocalDateRange.of(startDate, Period.ofYears(1))
                .stream()
                .filter(date -> isNotWeekend(date) && !holidays.contains(date))
                // Skip the first days of working days
                .skip(days)
                // Take the first suitable date
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Date not found in range"));
    }
}