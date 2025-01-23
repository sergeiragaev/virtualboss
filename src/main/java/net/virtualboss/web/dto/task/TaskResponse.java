package net.virtualboss.web.dto.task;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import net.virtualboss.web.dto.CustomFieldsAndLists;
import net.virtualboss.web.dto.contact.ContactResponse;
import net.virtualboss.web.dto.job.JobResponse;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

@Builder
@Data
@JsonFilter("TaskResponseFilter")
public class TaskResponse {
    @JsonProperty("TaskId")
    private UUID id;

    @JsonProperty("TaskNumber")
    private Long number;

    @JsonProperty("TaskDescription")
    private String description;

    @JsonProperty("TaskTargetStart")
    private LocalDate targetStart;

    @JsonProperty("TaskDuration")
    @Builder.Default
    private Integer duration = 0;

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
    private String marked = "False";

    @JsonProperty("JobNumber")
    @Builder.Default
    private String jobNumber = "";

    @JsonProperty("JobId")
    @Builder.Default
    private String jobId = "";

    @JsonProperty("ContactPerson")
    @Builder.Default
    private String contactPerson = "";

    @JsonProperty("ContactId")
    private String contactId;

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
    private Integer finishPlus = 1;

    @JsonProperty("TaskDeleted")
    private Boolean isDeleted;

    @JsonProperty("Job")
    @Builder.Default
    private JobResponse jobResponse = JobResponse.builder().build();

    @JsonProperty("Contact")
    @Builder.Default
    private ContactResponse contactResponse = ContactResponse.builder().build();

    @JsonProperty("CustomFieldsAndLists")
    @Builder.Default
    private CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();

    public static Map<String, Object> getFieldsMap(TaskResponse taskResponse, Set<String> fieldList) {

        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : taskResponse.getClass().getDeclaredFields()) {
            String captionValue;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
            }

            switch (captionValue) {
                case "Contact" -> {
                    if (taskResponse.contactResponse == null) continue;
                    responseMap.putAll(ContactResponse.getFieldsMap(taskResponse.contactResponse, fieldList));
                    continue;
                }
                case "Job" -> {
                    if (taskResponse.jobResponse == null) continue;
                    responseMap.putAll(JobResponse.getFieldsMap(taskResponse.jobResponse, fieldList));
                    continue;
                }
                case "CustomFieldsAndLists" -> {
                    if (taskResponse.customFieldsAndLists == null) continue;
                    responseMap.putAll(CustomFieldsAndLists.getFieldsMap(taskResponse.customFieldsAndLists, "Task", fieldList));
                    continue;
                }
            }

            if (fieldList == null || fieldList.contains(captionValue)) {
                try {
                    Object value = field.get(taskResponse);
                    if (value != null) responseMap.put(captionValue, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to access field: " + field.getName(), e);
                }
            }
        }
        return responseMap;
    }
}
