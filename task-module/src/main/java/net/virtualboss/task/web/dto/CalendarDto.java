package net.virtualboss.task.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class CalendarDto {
    private String title;
    private LocalDate start;
    private LocalDate end;
    private String color;
    private String job;
    private String person;
    private String taskNum;
    private String duration;
    private String taskNotes;
    private String url;
    @JsonProperty("TaskId")
    private String taskId;
}
