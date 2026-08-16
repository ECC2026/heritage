package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("inheritor")
public class Inheritor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String idCard;
    private String skillType;
    private String skillDesc;
    private String experience;
    private String certificate;
    private String 作品展示;
    private Integer auditStatus;
    private String auditRemark;
    private LocalDateTime auditTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
