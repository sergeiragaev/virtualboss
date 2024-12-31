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

    public static Map<String, Object> getFieldsMap(ContactResponse contactResponse, List<String> fieldList) {

        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : contactResponse.getClass().getDeclaredFields()) {
            String captionValue;
            if (field.isAnnotationPresent(JsonProperty.class)) {
                captionValue = field.getAnnotation(JsonProperty.class).value();
            } else {
                captionValue = field.getName();
            }

            if (captionValue.equals("ContactPerson")) {
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
