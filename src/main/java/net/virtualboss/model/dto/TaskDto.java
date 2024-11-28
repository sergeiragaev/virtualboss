package net.virtualboss.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
public class TaskDto {
    @JsonProperty("TaskID")
    private UUID id;

    @JsonProperty("TaskDescription")
    private String description;

    @JsonProperty("TaskTargetStart")
    private LocalDate targetStart;

    @JsonProperty("TaskDuration")
    private short duration;

    @JsonProperty("TaskTargetFinish")
    private LocalDate targetFinish;

    @JsonProperty("TaskActualFinish")
    private LocalDate actualFinish;

    @JsonProperty("TaskStatus")
    @Builder.Default
    private String status = "";

    @JsonProperty("TaskOrder")
    private String order;

    @JsonProperty("TaskNotes")
    @Builder.Default
    private String notes = "";

    @JsonProperty("TaskMarked")
    @Builder.Default
    private Boolean marked = Boolean.FALSE;

    @JsonProperty("JobNumber")
    @Builder.Default
    private String jobNumber = "";

    @JsonProperty("ContactPerson")
    @Builder.Default
    private String contactPerson = "";

    @JsonProperty("TaskRequested")
    @Builder.Default
    private String employee = "";

    @JsonProperty("TaskFiles")
    @Builder.Default
    private String files = "";

    @JsonProperty("TaskGroups")
    @Builder.Default
    private String groups = "";

    @JsonProperty("TaskFollows")
    @Builder.Default
    private String follows = "";

}
