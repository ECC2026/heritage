package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("performance")
public class Performance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String cover;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String organizer;
    private String performer;
    private BigDecimal price;
    private Integer seats;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
