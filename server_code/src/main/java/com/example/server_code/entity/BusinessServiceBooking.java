package com.example.server_code.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务服务预约（一期最小预约闭环，无真实支付）。
 * status: 1-已预约, 2-已取消, 3-已完成
 */
@Data
@TableName("business_service_booking")
public class BusinessServiceBooking {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long serviceId;
    private Long scheduleId;
    private Long userId;
    private Integer quantity;
    private String contactName;
    private String contactPhone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String serviceName;

    @TableField(exist = false)
    private String serviceCover;

    @TableField(exist = false)
    private String serviceProvider;

    @TableField(exist = false)
    private LocalDateTime scheduleStartTime;

    @TableField(exist = false)
    private LocalDateTime scheduleEndTime;

    @TableField(exist = false)
    private String statusText;
}
