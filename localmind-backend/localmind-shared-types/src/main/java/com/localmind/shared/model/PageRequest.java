package com.localmind.shared.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest {
    private int page = 0;
    private int size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
