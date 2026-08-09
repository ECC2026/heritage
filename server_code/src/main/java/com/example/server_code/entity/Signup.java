package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("signup")
public class Signup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long userId;
    private String userName;
    private String phone;
    private String remark;
    private Integer status;
    private LocalDateTime auditTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String activityName;

    @TableField(exist = false)
    private String activityCover;

    @TableField(exist = false)
    private String activityLocation;

    @TableField(exist = false)
    private LocalDateTime activityStartTime;

    @TableField(exist = false)
    private LocalDateTime activityEndTime;
}
