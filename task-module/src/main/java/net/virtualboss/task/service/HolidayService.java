package net.virtualboss.task.service;

import lombok.RequiredArgsConstructor;
import net.virtualboss.common.repository.projection.HolidayProjection;
import net.virtualboss.common.repository.HolidayRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HolidayService {
    private final HolidayRepository holidayRepository;

    public Set<LocalDate> getHolidays(int year, String countryCode) {
        return holidayRepository.findHolidays(year, countryCode).stream()
                .map(HolidayProjection::getDate)
                .collect(Collectors.toSet());
    }
}