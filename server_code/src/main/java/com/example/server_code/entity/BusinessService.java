package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务服务（康养陪伴 / 民俗演艺等）。
 * 与 Spring 的 Service 注解语义不同，因此命名为 BusinessService。
 */
@Data
@TableName("business_service")
public class BusinessService {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long productSystemId;
    private String cover;
    private String images;
    private String summary;
    private String description;
    private String providerName;
    private String location;
    private BigDecimal price;
    private String unit;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String productSystem;

    @TableField(exist = false)
    private List<BusinessServiceSchedule> schedules;
}
