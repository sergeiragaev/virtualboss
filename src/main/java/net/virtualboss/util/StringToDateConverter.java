package net.virtualboss.util;

import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class StringToDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    public LocalDate convert(@Nonnull String source) {
        try {
            return LocalDate.parse(source, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}