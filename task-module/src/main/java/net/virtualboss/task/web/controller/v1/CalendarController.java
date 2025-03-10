package net.virtualboss.task.web.controller.v1;

import lombok.RequiredArgsConstructor;
import net.virtualboss.task.web.dto.CalendarDto;
import net.virtualboss.task.service.CalendarService;
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
