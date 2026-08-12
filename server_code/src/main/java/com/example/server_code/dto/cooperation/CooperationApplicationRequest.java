package com.example.server_code.dto.cooperation;

import lombok.Data;

/**
 * B端合作申请提交请求。
 * 不允许携带 status/remark：状态默认 0（待处理），备注仅后台可填。
 */
@Data
public class CooperationApplicationRequest {
    private String companyName;
    private String contactName;
    private String contactPhone;
    private String cooperationType;
    private String requirement;
}
