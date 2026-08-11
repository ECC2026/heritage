package com.example.server_code.dto.home;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySummaryDto {
    private Long id;
    private String name;
    private String cover;
    private String summary;
    private String cityCode;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String organizerName;
    private Integer limitCount;
    private Integer signupCount;
    private Integer remaining;
}
