package net.virtualboss.job.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.virtualboss.common.annotation.EntityMapping;
import net.virtualboss.common.annotation.Flatten;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;

import java.lang.reflect.Field;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {
    @JsonProperty("JobId")
    @EntityMapping
    private UUID id;

    @JsonProperty("JobNumber")
    @EntityMapping
    private String number;

    @JsonProperty("JobLot")
    @Builder.Default
    @EntityMapping
    private String lot = "";

    @JsonProperty("JobSubdivision")
    @Builder.Default
    @EntityMapping
    private String subdivision = "";

    @JsonProperty("JobOwnerName")
    @Builder.Default
    @EntityMapping
    private String ownerName = "";

    @JsonProperty("JobLockBox")
    @Builder.Default
    @EntityMapping
    private String lockBox = "";

    @JsonProperty("JobDirections")
    @Builder.Default
    @EntityMapping
    private String directions = "";

    @JsonProperty("JobNotes")
    @Builder.Default
    @EntityMapping
    private String notes = "";

    @JsonProperty("JobAddress1")
    @Builder.Default
    @EntityMapping
    private String address1 = "";

    @JsonProperty("JobAddress2")
    @Builder.Default
    @EntityMapping
    private String address2 = "";

    @JsonProperty("JobCity")
    @Builder.Default
    @EntityMapping
    private String city = "";

    @JsonProperty("JobState")
    @Builder.Default
    @EntityMapping
    private String state = "";

    @JsonProperty("JobPostal")
    @Builder.Default
    @EntityMapping
    private String postal = "";

    @JsonProperty("JobHomePhone")
    @Builder.Default
    @EntityMapping
    private String homePhone = "";

    @JsonProperty("JobWorkPhone")
    @Builder.Default
    @EntityMapping
    private String workPhone = "";

    @JsonProperty("JobCellPhone")
    @Builder.Default
    @EntityMapping
    private String cellPhone = "";

    @JsonProperty("JobFax")
    @Builder.Default
    @EntityMapping
    private String fax = "";

    @JsonProperty("JobCompany")
    @Builder.Default
    @EntityMapping
    private String company = "";

    @JsonProperty("JobEmail")
    @Builder.Default
    @EntityMapping
    private String email = "";

    @JsonProperty("JobCountry")
    @Builder.Default
    @EntityMapping
    private String country = "";

    @JsonProperty("JobCustomFieldsAndLists")
    @Flatten(prefix = "Job")
    @Builder.Default
    @EntityMapping(path = "customFieldsAndListsValues")
    private CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();

    @JsonProperty("JobGroups")
    @Builder.Default
    private String groups = "";

    @JsonProperty("JobDeleted")
    @Builder.Default
    private Boolean isDeleted = false;


    public static Map<String, Object> getFieldsMap(JobResponse jobResponse, Set<String> fieldList) {
        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : jobResponse.getClass().getDeclaredFields()) {
            String fieldCaption = getFieldCaption(field);

            if (processSpecialField(fieldCaption, jobResponse, responseMap, fieldList)) {
                continue;
            }

            if (shouldIncludeField(fieldCaption, fieldList)) {
                addFieldToMap(jobResponse, responseMap, field, fieldCaption);
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
                                               JobResponse jobResponse,
                                               Map<String, Object> responseMap,
                                               Set<String> fieldList) {

        if ("JobCustomFieldsAndLists".equals(fieldCaption)) {
            processCustomFields(jobResponse, responseMap, fieldList);
            return true;
        }

        return false;
    }

    private static void processCustomFields(JobResponse jobResponse,
                                            Map<String, Object> responseMap,
                                            Set<String> fieldList) {
        Optional.ofNullable(jobResponse.customFieldsAndLists)
                .ifPresent(customFields -> responseMap.putAll(
                        CustomFieldsAndLists.getFieldsMap(customFields, "Job", fieldList)
                ));
    }

    private static boolean shouldIncludeField(String fieldCaption, Set<String> fieldList) {
        return fieldList == null || fieldList.contains(fieldCaption);
    }

    private static void addFieldToMap(JobResponse jobResponse,
                                      Map<String, Object> responseMap,
                                      Field field,
                                      String fieldCaption) {
        try {
            Optional.ofNullable(field.get(jobResponse))
                    .ifPresent(value -> responseMap.put(fieldCaption, value));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access field: " + field.getName(), e);
        }
    }
}
