package net.virtualboss.web.dto.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Filter extends PageFilter {
    @JsonProperty("FindString")
    protected String findString;
    @JsonProperty("MatchType")
    protected Integer matchType;
    @JsonProperty("Field")
    protected String field;
}
