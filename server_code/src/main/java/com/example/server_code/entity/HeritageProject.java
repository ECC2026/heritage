package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("heritage_project")
public class HeritageProject {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String cover;
    private String category;
    private String description;
    private String region;
    private String level;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
