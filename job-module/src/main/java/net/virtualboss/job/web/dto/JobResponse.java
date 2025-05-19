package net.virtualboss.job.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.virtualboss.common.annotation.EntityMapping;
import net.virtualboss.common.annotation.Flatten;
import net.virtualboss.common.web.dto.CustomFieldsAndLists;
import net.virtualboss.contact.web.dto.ContactResponse;

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

    @Builder.Default
    @EntityMapping
    @Flatten
    private ContactResponse owner = ContactResponse.builder().build();
}
