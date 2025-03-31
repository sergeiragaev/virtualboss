package net.virtualboss.task.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.virtualboss.common.annotation.EntityMapping;
import net.virtualboss.common.annotation.Flatten;
import net.virtualboss.common.model.enums.TaskStatus;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.web.dto.ContactResponse;
import net.virtualboss.job.web.dto.JobResponse;

import java.time.LocalDate;
import java.util.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    @JsonProperty("TaskId")
    @EntityMapping
    private UUID id;

    @JsonProperty("TaskNumber")
    @EntityMapping
    private Long number;

    @JsonProperty("TaskDescription")
    @EntityMapping
    private String description;

    @JsonProperty("TaskTargetStart")
    @EntityMapping
    private LocalDate targetStart;

    @JsonProperty("TaskDuration")
    @Builder.Default
    @EntityMapping
    private Integer duration = 0;

    @JsonProperty("TaskTargetFinish")
    @EntityMapping
    private LocalDate targetFinish;

    @JsonProperty("TaskActualFinish")
    @EntityMapping
    private LocalDate actualFinish;

    @JsonProperty("TaskStatus")
    @EntityMapping
    private TaskStatus status;

    @JsonProperty("TaskOrder")
    @EntityMapping
    private String taskOrder;

    @JsonProperty("TaskNotes")
    @Builder.Default
    @EntityMapping
    private String notes = "";

    @JsonProperty("TaskMarked")
    @Builder.Default
    @EntityMapping
    private String marked = "False";

    @JsonProperty("JobNumber")
    @Builder.Default
    private String jobNumber = "";

    @JsonProperty("JobId")
    @EntityMapping
    private UUID jobId;

    @JsonProperty("ContactId")
    @EntityMapping
    private UUID contactId;

    @JsonProperty("TaskRequested")
    @Builder.Default
    private String requested = "";

    @JsonProperty("TaskFiles")
    @Builder.Default
    private String files = "";

    @JsonProperty("TaskGroups")
    @Builder.Default
    private String groups = "";

    @JsonProperty("TaskFollows")
    @Builder.Default
    private String follows = "";

    @JsonProperty("FinishPlus")
    @Builder.Default
    @EntityMapping
    private Integer finishPlus = 1;

    @JsonProperty("TaskDeleted")
    private Boolean isDeleted;

    @Builder.Default
    @EntityMapping
    @Flatten
    private JobResponse job = JobResponse.builder().build();

    @Builder.Default
    @EntityMapping
    @Flatten
    private ContactResponse contact = ContactResponse.builder().build();

    @JsonProperty("TaskCustomFieldsAndLists")
    @Builder.Default
    @Flatten(prefix = "Task")
    @EntityMapping(path = "customFieldsAndListsValues")
    private CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();
}
