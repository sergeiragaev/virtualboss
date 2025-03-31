package net.virtualboss.common.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.virtualboss.common.annotation.EntityMapping;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFieldsAndLists {
    @JsonProperty("CustomField1")
    @Builder.Default
    @EntityMapping
    private String customField1 = "";
    @JsonProperty("CustomField2")
    @Builder.Default
    @EntityMapping
    private String customField2 = "";
    @JsonProperty("CustomField3")
    @Builder.Default
    @EntityMapping
    private String customField3 = "";
    @JsonProperty("CustomField4")
    @Builder.Default
    @EntityMapping
    private String customField4 = "";
    @JsonProperty("CustomField5")
    @Builder.Default
    @EntityMapping
    private String customField5 = "";
    @JsonProperty("CustomField6")
    @Builder.Default
    @EntityMapping
    private String customField6 = "";

    @JsonProperty("CustomList1")
    @Builder.Default
    @EntityMapping
    private String customList1 = "";
    @JsonProperty("CustomList2")
    @Builder.Default
    @EntityMapping
    private String customList2 = "";
    @JsonProperty("CustomList3")
    @Builder.Default
    @EntityMapping
    private String customList3 = "";
    @JsonProperty("CustomList4")
    @Builder.Default
    @EntityMapping
    private String customList4 = "";
    @JsonProperty("CustomList5")
    @Builder.Default
    @EntityMapping
    private String customList5 = "";
    @JsonProperty("CustomList6")
    @Builder.Default
    @EntityMapping
    private String customList6 = "";
}
