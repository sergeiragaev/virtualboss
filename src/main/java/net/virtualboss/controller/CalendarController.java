package net.virtualboss.controller;

import lombok.RequiredArgsConstructor;
import net.virtualboss.model.dto.CalendarDto;
import net.virtualboss.service.CalendarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService service;

    @GetMapping("/calendarfeed")
    public ResponseEntity<List<CalendarDto>> contactFeed() {
        return ResponseEntity.ok(service.findAll());
    }

}
