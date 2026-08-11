package com.example.server_code.dto.home;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDto {
    private Long id;
    private String code;
    private String name;
    private String icon;
    private String description;
    private Long parentId;
    private Integer sort;
    private List<CategoryDto> children = new ArrayList<>();
}
