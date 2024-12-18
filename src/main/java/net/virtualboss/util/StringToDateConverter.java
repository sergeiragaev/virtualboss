package net.virtualboss.util;

import jakarta.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class StringToDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter FORMATTER_USA = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter FORMATTER_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate convert(@Nonnull String source) {
        try {
            return LocalDate.parse(source, FORMATTER_USA);
        } catch (Exception e) {
            try {
                return LocalDate.parse(source, FORMATTER_ISO);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}