package net.virtualboss.contact.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.virtualboss.common.annotation.EntityMapping;
import net.virtualboss.common.annotation.Flatten;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;

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

    /**
     * Вычисляемое поле "ContactPerson", объединяющее имя и фамилию.
     */
    public String getPerson() {
        String fullName = firstName;
        if (!firstName.isBlank() && !lastName.isBlank()) {
            fullName += " " + lastName;
        }
        return fullName;
    }
}
