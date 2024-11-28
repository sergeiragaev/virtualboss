package net.virtualboss.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class JobDto {
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
}
