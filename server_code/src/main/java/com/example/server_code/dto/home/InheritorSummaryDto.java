package com.example.server_code.dto.home;

import lombok.Data;

@Data
public class InheritorSummaryDto {
    private Long id;
    private String displayName;
    private String portrait;
    private String profile;
    private String skillType;
    private String regionCode;
}
