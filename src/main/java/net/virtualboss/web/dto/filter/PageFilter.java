package net.virtualboss.web.dto.filter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PageFilter {
    private Integer size;
    private Integer page;
    private String sort;
}
