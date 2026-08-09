package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String cover;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long organizerId;
    private String organizerName;
    private Integer limitCount;
    private Integer signupCount;
    private Integer status;
    private Integer type;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String title;

    @TableField(exist = false)
    private Integer maxParticipants;

    @TableField(exist = false)
    private String statusText;

    @TableField(exist = false)
    private String organizer;
}
