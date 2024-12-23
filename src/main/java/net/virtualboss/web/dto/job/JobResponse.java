package net.virtualboss.web.dto.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class JobResponse implements Serializable {
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

    @JsonProperty("JobCustomField1")
    @Builder.Default
    private String customField1 = "";
    @JsonProperty("JobCustomField2")
    @Builder.Default
    private String customField2 = "";
    @JsonProperty("JobCustomField3")
    @Builder.Default
    private String customField3 = "";
    @JsonProperty("JobCustomField4")
    @Builder.Default
    private String customField4 = "";
    @JsonProperty("JobCustomField5")
    @Builder.Default
    private String customField5 = "";
    @JsonProperty("JobCustomField6")
    @Builder.Default
    private String customField6 = "";

    @JsonProperty("JobCustomList1")
    @Builder.Default
    private String customList1 = "";
    @JsonProperty("JobCustomList2")
    @Builder.Default
    private String customList2 = "";
    @JsonProperty("JobCustomList3")
    @Builder.Default
    private String customList3 = "";
    @JsonProperty("JobCustomList4")
    @Builder.Default
    private String customList4 = "";
    @JsonProperty("JobCustomList5")
    @Builder.Default
    private String customList5 = "";
    @JsonProperty("JobCustomList6")
    @Builder.Default
    private String customList6 = "";
    
    public static Map<String, Object> getFieldsMap(JobResponse jobResponse, List<String> fieldList) {

        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : jobResponse.getClass().getDeclaredFields()) {
            String captionValue;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
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
