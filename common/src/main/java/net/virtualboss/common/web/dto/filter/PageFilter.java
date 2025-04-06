package net.virtualboss.common.web.dto.filter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PageFilter {
    private Integer limit;
    private Integer page;
    private String sort;
}
