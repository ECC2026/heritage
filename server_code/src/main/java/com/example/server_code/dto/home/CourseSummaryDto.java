package com.example.server_code.dto.home;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseSummaryDto {
    private Long id;
    private String title;
    private String cover;
    private String summary;
    private Long inheritorId;
    private String inheritorName;
    private Long heritageProjectId;
    private Long categoryId;
    private String category;
    private String serviceMode;
    private BigDecimal price;
    private Integer durationMinutes;
    private String cityCode;
    private String location;
    private Long nextSessionId;
    private LocalDateTime nextStartTime;
    private LocalDateTime nextEndTime;
    private Integer remaining;
}
