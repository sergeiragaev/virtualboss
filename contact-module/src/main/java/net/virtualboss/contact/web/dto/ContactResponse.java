package net.virtualboss.contact.web.dto;

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
public class ContactResponse {
    @JsonProperty("ContactId")
    @EntityMapping
    private UUID id;

    @JsonProperty("ContactCompany")
    @EntityMapping
    @Builder.Default
    private String company = "";

    @JsonProperty("ContactProfession")
    @EntityMapping
    @Builder.Default
    private String profession = "";

    @JsonProperty("ContactPerson")
    @Builder.Default
    private String person = "";

    public String getPerson() {
        return this.firstName + ((this.firstName.isBlank()) ? "" : " ") + this.lastName;
    }

    @JsonProperty("ContactFirstName")
    @EntityMapping
    @Builder.Default
    private String firstName = "";

    @JsonProperty("ContactLastName")
    @EntityMapping
    @Builder.Default
    private String lastName = "";

    @JsonProperty("ContactSupervisor")
    @EntityMapping
    @Builder.Default
    private String supervisor = "";

    @JsonProperty("ContactSpouse")
    @EntityMapping
    @Builder.Default
    private String spouse = "";

    @JsonProperty("ContactTaxID")
    @EntityMapping
    @Builder.Default
    private String taxId = "";

    @JsonProperty("ContactWebSite")
    @EntityMapping
    @Builder.Default
    private String webSite = "";

    @JsonProperty("ContactWorkersCompDate")
    @EntityMapping
    @Builder.Default
    private String workersCompDate = "";

    @JsonProperty("ContactInsuranceDate")
    @EntityMapping
    @Builder.Default
    private String insuranceDate = "";

    @JsonProperty("ContactComments")
    @EntityMapping
    @Builder.Default
    private String comments = "";

    @JsonProperty("ContactNotes")
    @EntityMapping
    @Builder.Default
    private String notes = "";

    @JsonProperty("ContactFax")
    @EntityMapping
    @Builder.Default
    private String fax = "";

    @JsonProperty("ContactEmail")
    @EntityMapping
    @Builder.Default
    private String email = "";

    @JsonProperty("ContactPhones")
    @EntityMapping
    @Builder.Default
    private String phones = "";

    @JsonProperty("ContactDeleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @JsonProperty("ContactCustomFieldsAndLists")
    @Builder.Default
    @Flatten(prefix = "Contact")
    @EntityMapping(path = "customFieldsAndListsValues")
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

        if ("ContactCustomFieldsAndLists".equals(fieldCaption)) {
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
