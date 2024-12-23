package net.virtualboss.web.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import net.virtualboss.web.dto.contact.ContactResponse;
import net.virtualboss.web.dto.job.JobResponse;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;

@Builder
@Data
public class TaskResponse implements Serializable {
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
    private Short duration = 0;

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

    @JsonProperty("TaskCustomField1")
    @Builder.Default
    private String customField1 = "";
    @JsonProperty("TaskCustomField2")
    @Builder.Default
    private String customField2 = "";
    @JsonProperty("TaskCustomField3")
    @Builder.Default
    private String customField3 = "";
    @JsonProperty("TaskCustomField4")
    @Builder.Default
    private String customField4 = "";
    @JsonProperty("TaskCustomField5")
    @Builder.Default
    private String customField5 = "";
    @JsonProperty("TaskCustomField6")
    @Builder.Default
    private String customField6 = "";

    @JsonProperty("TaskCustomList1")
    @Builder.Default
    private String customList1 = "";
    @JsonProperty("TaskCustomList2")
    @Builder.Default
    private String customList2 = "";
    @JsonProperty("TaskCustomList3")
    @Builder.Default
    private String customList3 = "";
    @JsonProperty("TaskCustomList4")
    @Builder.Default
    private String customList4 = "";
    @JsonProperty("TaskCustomList5")
    @Builder.Default
    private String customList5 = "";
    @JsonProperty("TaskCustomList6")
    @Builder.Default
    private String customList6 = "";


    @JsonProperty("Job")
    @Builder.Default
    private JobResponse jobResponse = JobResponse.builder().build();

    @JsonProperty("Contact")
    @Builder.Default
    private ContactResponse contactResponse = ContactResponse.builder().build();

    public static Map<String, Object> getFieldsMap(TaskResponse taskResponse, List<String> fieldList) {

        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : taskResponse.getClass().getDeclaredFields()) {
            String captionValue;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
            }

            if (captionValue.equals("Contact")) {
                if (taskResponse.contactResponse == null) continue;
                responseMap.putAll(ContactResponse.getFieldsMap(taskResponse.contactResponse, fieldList));
                continue;
            }

            if (captionValue.equals("Job")) {
                if (taskResponse.jobResponse == null) continue;
                responseMap.putAll(JobResponse.getFieldsMap(taskResponse.jobResponse, fieldList));
                continue;
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
