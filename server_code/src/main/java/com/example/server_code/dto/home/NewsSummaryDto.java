package com.example.server_code.dto.home;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewsSummaryDto {
    private Long id;
    private String title;
    private String cover;
    private String summary;
    private String category;
    private String author;
    private String source;
    private LocalDateTime createTime;
}
