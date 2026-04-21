package com.euro.db2performance.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meta {
    private Integer page;
    private Integer pageSize;
    private Long totalItems;
    private Integer totalPages;
}
