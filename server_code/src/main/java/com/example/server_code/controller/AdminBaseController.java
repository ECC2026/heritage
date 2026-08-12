package com.example.server_code.controller;

import com.example.server_code.common.BizException;
import com.example.server_code.utils.AdminAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 管理端 Controller 基类：所有管理接口必须先通过现有管理员认证。
 */
public abstract class AdminBaseController {

    @Autowired
    private AdminAuthUtil adminAuthUtil;

    /**
     * 校验请求为有效启用的管理员，否则抛出 401 业务异常。
     */
    protected void requireAdmin(HttpServletRequest request) {
        if (adminAuthUtil.getCurrentAdmin(request) == null) {
            throw new BizException(401, "请先登录");
        }
    }
}
