package net.virtualboss.web.dto.task;

import lombok.Builder;
import lombok.Data;
import net.virtualboss.model.enums.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UpsertTaskRequest {
    private UUID id;
    private String description;
    private LocalDate targetStart;
    private Integer duration;
    private LocalDate actualFinish;
    private TaskStatus status;
    private String order;
    private String notes;
    private Boolean marked;
    private Integer finishPlus;
}
