package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String title;
    private String content;
    private String images;
    private String category;
    private Integer views;
    private Integer likes;
    private Integer comments;
    private Integer status;
    private Integer isTop;
    private Integer isEssence;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
