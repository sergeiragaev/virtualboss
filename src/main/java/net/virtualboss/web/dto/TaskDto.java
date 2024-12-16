package net.virtualboss.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

@Builder
@Data
public class TaskDto implements Serializable {
    @JsonProperty("TaskId")
    private UUID id;

    @JsonProperty("TaskDescription")
    private String description;

    @JsonProperty("TaskTargetStart")
    private LocalDate targetStart;

    @JsonProperty("TaskDuration")
    @Builder.Default
    private Short duration = 1;

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

    public static Map<String, Object> getFieldsMap(TaskDto taskDto, boolean useJsonCaption, List<String> fieldList) {

        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : TaskDto.class.getDeclaredFields()) {
            String captionValue;
            if (useJsonCaption && field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
            }
            if (fieldList == null || fieldList.contains(captionValue)) {
                try {
                    Object value = field.get(taskDto);
                    if (value != null) responseMap.put(captionValue, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to access field: " + field.getName(), e);
                }
            }
        }
        return responseMap;
    }
}
