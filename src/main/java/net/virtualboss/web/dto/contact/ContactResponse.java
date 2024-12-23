package net.virtualboss.web.dto.contact;

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
public class ContactResponse implements Serializable {
    @JsonProperty("ContactId")
    private UUID id;

    @JsonProperty("ContactCompany")
    @Builder.Default
    private String company = "";

    @JsonProperty("ContactProfession")
    @Builder.Default
    private String profession = "";

    @JsonProperty("ContactPerson")
    private String person;

    public String getPerson() {
        return firstName + " " + lastName;
    }

    @JsonProperty("ContactFirstName")
    @Builder.Default
    private String firstName = "";

    @JsonProperty("ContactLastName")
    @Builder.Default
    private String lastName = "";

    @JsonProperty("ContactSupervisor")
    @Builder.Default
    private String supervisor = "";

    @JsonProperty("ContactSpouse")
    @Builder.Default
    private String spouse = "";

    @JsonProperty("ContactTaxID")
    @Builder.Default
    private String taxId = "";

    @JsonProperty("ContactWebSite")
    @Builder.Default
    private String webSite = "";

    @JsonProperty("ContactWorkersCompDate")
    @Builder.Default
    private String workersCompDate = "";

    @JsonProperty("ContactInsuranceDate")
    @Builder.Default
    private String insuranceDate = "";

    @JsonProperty("ContactComments")
    @Builder.Default
    private String comments = "";

    @JsonProperty("ContactNotes")
    @Builder.Default
    private String notes = "";

    @JsonProperty("ContactFax")
    @Builder.Default
    private String fax = "";

    @JsonProperty("ContactEmail")
    @Builder.Default
    private String email = "";

    @JsonProperty("ContactPhones")
    @Builder.Default
    private String phones = "";

    @JsonProperty("ContactCustomField1")
    @Builder.Default
    private String customField1 = "";
    @JsonProperty("ContactCustomField2")
    @Builder.Default
    private String customField2 = "";
    @JsonProperty("ContactCustomField3")
    @Builder.Default
    private String customField3 = "";
    @JsonProperty("ContactCustomField4")
    @Builder.Default
    private String customField4 = "";
    @JsonProperty("ContactCustomField5")
    @Builder.Default
    private String customField5 = "";
    @JsonProperty("ContactCustomField6")
    @Builder.Default
    private String customField6 = "";

    @JsonProperty("ContactCustomList1")
    @Builder.Default
    private String customList1 = "";
    @JsonProperty("ContactCustomList2")
    @Builder.Default
    private String customList2 = "";
    @JsonProperty("ContactCustomList3")
    @Builder.Default
    private String customList3 = "";
    @JsonProperty("ContactCustomList4")
    @Builder.Default
    private String customList4 = "";
    @JsonProperty("ContactCustomList5")
    @Builder.Default
    private String customList5 = "";
    @JsonProperty("ContactCustomList6")
    @Builder.Default
    private String customList6 = "";

    public static Map<String, Object> getFieldsMap(ContactResponse contactResponse, List<String> fieldList) {

        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : contactResponse.getClass().getDeclaredFields()) {
            String captionValue;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
            }

            if (captionValue.equals("ContactPerson") && fieldList.contains(captionValue)) {
                Object value = contactResponse.getPerson();
                if (value != null) responseMap.put(captionValue, value);
                continue;
            }

            if (fieldList == null || fieldList.contains(captionValue)) {
                try {
                    Object value = field.get(contactResponse);
                    if (value != null) responseMap.put(captionValue, value);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Failed to access field: " + field.getName(), e);
                }
            }
        }
        return responseMap;
    }
}
