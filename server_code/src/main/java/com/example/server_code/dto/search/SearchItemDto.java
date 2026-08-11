package com.example.server_code.dto.search;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SearchItemDto {
    private String type;
    private Long id;
    private String title;
    private String cover;
    private String summary;
    private String category;
    private String levelCode;
    private String regionCode;
    private BigDecimal price;
    private LocalDateTime startTime;
}
