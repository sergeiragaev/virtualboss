package net.virtualboss.web.dto.contact;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import net.virtualboss.web.dto.CustomFieldsAndLists;

import java.lang.reflect.Field;
import java.util.*;

@Data
@Builder
public class ContactResponse {
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

    @JsonProperty("ContactDeleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @JsonProperty("CustomFieldsAndLists")
    @Builder.Default
    private CustomFieldsAndLists customFieldsAndLists = CustomFieldsAndLists.builder().build();

    @JsonProperty("ContactGroups")
    @Builder.Default
    private String groups = "";

    public static Map<String, Object> getFieldsMap(ContactResponse contactResponse, Set<String> fieldList) {
        Map<String, Object> responseMap = new HashMap<>();

        for (Field field : contactResponse.getClass().getDeclaredFields()) {
            String fieldCaption = getFieldCaption(field);

            if (processSpecialField(fieldCaption, contactResponse, responseMap, fieldList)) {
                continue;
            }

            if (shouldIncludeField(fieldCaption, fieldList)) {
                addFieldToMap(contactResponse, responseMap, field, fieldCaption);
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
                                               ContactResponse contactResponse,
                                               Map<String, Object> responseMap,
                                               Set<String> fieldList) {
        if ("ContactPerson".equals(fieldCaption)) {
            processContactPerson(contactResponse, responseMap);
            return true;
        }

        if ("CustomFieldsAndLists".equals(fieldCaption)) {
            processCustomFields(contactResponse, responseMap, fieldList);
            return true;
        }

        return false;
    }

    private static void processContactPerson(ContactResponse contactResponse, Map<String, Object> responseMap) {
        Optional.ofNullable(contactResponse.getPerson())
                .ifPresent(person -> responseMap.put("ContactPerson", person));
    }

    private static void processCustomFields(ContactResponse contactResponse,
                                            Map<String, Object> responseMap,
                                            Set<String> fieldList) {
        Optional.ofNullable(contactResponse.customFieldsAndLists)
                .ifPresent(customFields -> responseMap.putAll(
                        CustomFieldsAndLists.getFieldsMap(customFields, "Contact", fieldList)
                ));
    }

    private static boolean shouldIncludeField(String fieldCaption, Set<String> fieldList) {
        return fieldList == null || fieldList.contains(fieldCaption);
    }

    private static void addFieldToMap(ContactResponse contactResponse,
                                      Map<String, Object> responseMap,
                                      Field field,
                                      String fieldCaption) {
        try {
            Optional.ofNullable(field.get(contactResponse))
                    .ifPresent(value -> responseMap.put(fieldCaption, value));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access field: " + field.getName(), e);
        }
    }
}
