package net.virtualboss.web.dto.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import net.virtualboss.web.dto.CustomFieldsAndLists;

import java.lang.reflect.Field;
import java.util.*;

@Data
@Builder
public class JobResponse {
    @JsonProperty("JobId")
    private UUID id;

    @JsonProperty("JobNumber")
    private String number;

    @JsonProperty("JobLot")
    @Builder.Default
    private String lot = "";

    @JsonProperty("JobSubdivision")
    @Builder.Default
    private String subdivision = "";

    @JsonProperty("JobOwnerName")
    @Builder.Default
    private String ownerName = "";

    @JsonProperty("JobLockBox")
    @Builder.Default
    private String lockBox = "";

    @JsonProperty("JobDirections")
    @Builder.Default
    private String directions = "";

    @JsonProperty("JobNotes")
    @Builder.Default
    private String notes = "";

    @JsonProperty("JobAddress1")
    @Builder.Default
    private String address1 = "";

    @JsonProperty("JobAddress2")
    @Builder.Default
    private String address2 = "";

    @JsonProperty("JobCity")
    @Builder.Default
    private String city = "";

    @JsonProperty("JobState")
    @Builder.Default
    private String state = "";

    @JsonProperty("JobPostal")
    @Builder.Default
    private String postal = "";

    @JsonProperty("JobHomePhone")
    @Builder.Default
    private String homePhone = "";

    @JsonProperty("JobWorkPhone")
    @Builder.Default
    private String workPhone = "";

    @JsonProperty("JobCellPhone")
    @Builder.Default
    private String cellPhone = "";

    @JsonProperty("JobFax")
    @Builder.Default
    private String fax = "";

    @JsonProperty("JobCompany")
    @Builder.Default
    private String company = "";

    @JsonProperty("JobEmail")
    @Builder.Default
    private String email = "";

    @JsonProperty("JobCountry")
    @Builder.Default
    private String country = "";

    @JsonProperty("CustomFieldsAndLists")
    @Builder.Default
    private CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();

    @JsonProperty("JobGroups")
    @Builder.Default
    private String groups = "";


    public static Map<String, Object> getFieldsMap(JobResponse jobResponse, Set<String> fieldList) {

        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : jobResponse.getClass().getDeclaredFields()) {
            String captionValue;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
            }

            if (captionValue.equals("CustomFieldsAndLists")) {
                if (jobResponse.customFieldsAndLists == null) continue;
                responseMap.putAll(CustomFieldsAndLists.getFieldsMap(jobResponse.customFieldsAndLists, "Job", fieldList));
                continue;
            }

            if (fieldList == null || fieldList.contains(captionValue)) {
                try {
                    Object value = field.get(jobResponse);
                    if (value != null) responseMap.put(captionValue, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to access field: " + field.getName(), e);
                }
            }
        }
        return responseMap;
    }
}
