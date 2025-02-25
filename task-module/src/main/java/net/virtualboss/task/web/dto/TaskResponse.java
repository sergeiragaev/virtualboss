package net.virtualboss.task.web.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import net.virtualboss.common.model.enums.TaskStatus;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.web.dto.ContactResponse;
import net.virtualboss.job.web.dto.JobResponse;

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
    private TaskStatus status;

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
            case "Contact" -> {
                processContact(taskResponse, responseMap, fieldList);
                return true;
            }
            case "Job" -> {
                processJob(taskResponse, responseMap, fieldList);
                return true;
            }
            case "CustomFieldsAndLists" -> {
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
        Optional.ofNullable(taskResponse.contactResponse)
                .ifPresent(contact -> responseMap.putAll(
                        ContactResponse.getFieldsMap(contact, fieldList)
                ));
    }

    private static void processJob(TaskResponse taskResponse,
                                   Map<String, Object> responseMap,
                                   Set<String> fieldList) {
        Optional.ofNullable(taskResponse.jobResponse)
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
