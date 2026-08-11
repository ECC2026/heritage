package com.example.server_code.dto.home;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSummaryDto {
    private Long id;
    private String name;
    private String cover;
    private String summary;
    private Long categoryId;
    private String category;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer sales;
}
