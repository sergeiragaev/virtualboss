package net.virtualboss.task.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    @JsonProperty("ContactPerson")
    @Builder.Default
    private String contactPerson = "";

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

    public static Map<String, Object> getFieldsMap(TaskResponse taskResponse, Set<String> fieldList) {
        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : taskResponse.getClass().getDeclaredFields()) {
            String fieldCaption = getFieldCaption(field);

            if (processSpecialField(fieldCaption, taskResponse, responseMap, fieldList)) {
                continue;
            }

            if (shouldIncludeField(fieldCaption, fieldList)) {
                addFieldToMap(taskResponse, responseMap, field, fieldCaption);
            }
        }
        return responseMap;
    }

    private static String getFieldCaption(Field field) {
        return field.isAnnotationPresent(JsonProperty.class)
                ? field.getAnnotation(JsonProperty.class).value()
                : field.getName();
    }

    private static boolean processSpecialField(String fieldCaption,
                                               TaskResponse taskResponse,
                                               Map<String, Object> responseMap,
                                               Set<String> fieldList) {
        switch (fieldCaption) {
            case "contact" -> {
                processContact(taskResponse, responseMap, fieldList);
                return true;
            }
            case "job" -> {
                processJob(taskResponse, responseMap, fieldList);
                return true;
            }
            case "TaskCustomFieldsAndLists" -> {
                processCustomFields(taskResponse, responseMap, fieldList);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void processContact(TaskResponse taskResponse,
                                       Map<String, Object> responseMap,
                                       Set<String> fieldList) {
        Optional.ofNullable(taskResponse.contact)
                .ifPresent(contact -> responseMap.putAll(
                        ContactResponse.getFieldsMap(contact, fieldList)
                ));
    }

    private static void processJob(TaskResponse taskResponse,
                                   Map<String, Object> responseMap,
                                   Set<String> fieldList) {
        Optional.ofNullable(taskResponse.job)
                .ifPresent(job -> responseMap.putAll(
                        JobResponse.getFieldsMap(job, fieldList)
                ));
    }

    private static void processCustomFields(TaskResponse taskResponse,
                                            Map<String, Object> responseMap,
                                            Set<String> fieldList) {
        Optional.ofNullable(taskResponse.customFieldsAndLists)
                .ifPresent(customFields -> responseMap.putAll(
                        CustomFieldsAndLists.getFieldsMap(customFields, "Task", fieldList)
                ));
    }

    private static boolean shouldIncludeField(String fieldCaption, Set<String> fieldList) {
        return fieldList == null || fieldList.contains(fieldCaption);
    }

    private static void addFieldToMap(TaskResponse taskResponse,
                                      Map<String, Object> responseMap,
                                      Field field,
                                      String fieldCaption) {
        try {
            Optional.ofNullable(field.get(taskResponse))
                    .ifPresent(value -> responseMap.put(fieldCaption, value));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access field: " + field.getName(), e);
        }
    }
}
