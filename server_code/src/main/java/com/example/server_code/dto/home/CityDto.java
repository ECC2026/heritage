package com.example.server_code.dto.home;

import lombok.Data;

@Data
public class CityDto {
    private Long id;
    private String code;
    private String name;
    private String provinceCode;
    private String provinceName;
    private Integer isDefault;
}
