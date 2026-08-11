package com.example.server_code.dto.home;

import lombok.Data;

@Data
public class BannerDto {
    private Long id;
    private String title;
    private String image;
    private String link;
    private String linkType;
    private Long targetId;
    private Integer sort;
}
