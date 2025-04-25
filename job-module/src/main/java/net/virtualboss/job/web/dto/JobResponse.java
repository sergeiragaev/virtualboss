package net.virtualboss.job.web.dto;

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

    @JsonProperty("Color")
    @EntityMapping
    private String color;

    @JsonProperty("JobDeleted")
    @Builder.Default
    private Boolean isDeleted = false;
}
