package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务服务场次（一期仅基础场次，无复杂排班）。
 */
@Data
@TableName("business_service_schedule")
public class BusinessServiceSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long serviceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer capacity;
    private Integer bookedCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 剩余可预约人数（capacity - bookedCount），不入库。 */
    @TableField(exist = false)
    private Integer remaining;
}
