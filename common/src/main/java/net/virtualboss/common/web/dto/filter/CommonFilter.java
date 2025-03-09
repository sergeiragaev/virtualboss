package net.virtualboss.common.web.dto.filter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class CommonFilter extends PageFilter {
    @JsonProperty("FindString")
    protected String findString;
    @JsonProperty("MatchType")
    protected Integer matchType;
    @JsonProperty("Field")
    protected String field;
    @JsonProperty("IsDeleted")
    private Boolean isDeleted = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CommonFilter that = (CommonFilter) o;
        return Objects.equals(findString, that.findString) && Objects.equals(matchType, that.matchType) && Objects.equals(field, that.field) && Objects.equals(isDeleted, that.isDeleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), findString, matchType, field, isDeleted);
    }
}
