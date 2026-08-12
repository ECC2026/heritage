package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * B端合作申请。
 * status: 0-待处理, 1-已联系, 2-已完成, 3-已关闭
 */
@Data
@TableName("cooperation_application")
public class CooperationApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String companyName;
    private String contactName;
    private String contactPhone;
    private String cooperationType;
    private String requirement;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String cooperationTypeText;

    @TableField(exist = false)
    private String statusText;
}
