package com.example.server_code.dto.service;

import lombok.Data;

/**
 * 服务预约请求。userId 一律从登录态获取，绝不信赖前端传入。
 */
@Data
public class ServiceBookingRequest {
    private Long scheduleId;
    private Integer quantity;
    private String contactName;
    private String contactPhone;
}
