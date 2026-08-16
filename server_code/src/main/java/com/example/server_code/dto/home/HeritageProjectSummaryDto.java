package com.example.server_code.dto.home;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HeritageProjectSummaryDto {
    private Long id;
    private String name;
    private String cover;
    private String summary;
    private Long categoryId;
    private String category;
    private String levelCode;
    private String level;
    private String regionCode;
    private String region;
    private String officialCode;
    private String recognitionAuthority;
    private String recognitionBatch;
    private LocalDate recognizedAt;
}
