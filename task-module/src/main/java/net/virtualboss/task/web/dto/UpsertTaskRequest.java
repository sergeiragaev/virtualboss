package net.virtualboss.task.web.dto;

import lombok.Builder;
import lombok.Data;
import net.virtualboss.common.model.enums.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UpsertTaskRequest {
    private UUID id;
    private String description;
    private LocalDate targetStart;
    private Integer duration;
    private LocalDate targetFinish;
    private LocalDate actualFinish;
    private TaskStatus status;
    private String order;
    private String notes;
    private Boolean marked;
    private Integer finishPlus;
    @Builder.Default
    private String files = "";
}
