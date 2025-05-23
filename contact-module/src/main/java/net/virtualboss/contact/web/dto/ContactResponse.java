package net.virtualboss.contact.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.virtualboss.common.annotation.EntityMapping;
import net.virtualboss.common.annotation.Flatten;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;

import java.time.LocalDate;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    @JsonProperty("ContactId")
    @EntityMapping
    private UUID id;

    @Builder.Default
    @EntityMapping
    @Flatten
    private CompanyResponse company = CompanyResponse.builder().build();

    @Builder.Default
    @EntityMapping
    @Flatten
    private ProfessionResponse profession = ProfessionResponse.builder().build();

    @JsonProperty("ContactPerson")
    @EntityMapping
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
    private LocalDate workersCompDate;

    @JsonProperty("ContactInsuranceDate")
    @EntityMapping
    private LocalDate insuranceDate;

    @JsonProperty("ContactComments")
    @EntityMapping
    @Builder.Default
    private String comments = "";

    @JsonProperty("ContactNotes")
    @EntityMapping
    @Builder.Default
    private String notes = "";

    @JsonProperty("ContactEmail")
    @EntityMapping
    @Builder.Default
    private String email = "";

    @JsonProperty("ContactPhones")
    @Builder.Default
    @EntityMapping
    private String phones = "";

    @JsonProperty("ContactAddresses")
    @Builder.Default
    @EntityMapping
    private String addresses = "";

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

    @JsonProperty("Color")
    @EntityMapping
    private String color;

    public String getPerson() {
        String fullName = firstName;
        if (!firstName.isBlank() && !lastName.isBlank()) {
            fullName += " " + lastName;
        }
        return fullName;
    }
}
