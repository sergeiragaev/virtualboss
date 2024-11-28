package net.virtualboss.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ContactDto {
    @JsonProperty("ContactId")
    private UUID id;

    @JsonProperty("ContactCompany")
    @Builder.Default
    private String company = "";

    @JsonProperty("ContactProfession")
    @Builder.Default
    private String profession = "";

    @JsonProperty("ContactPerson")
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
    private LocalDate workersCompDate = LocalDate.MIN;

    @JsonProperty("ContactInsuranceDate")
    @Builder.Default
    private LocalDate insuranceDate = LocalDate.MIN;

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
}
