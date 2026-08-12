package com.example.server_code.utils;

import com.example.server_code.entity.Admin;
import com.example.server_code.mapper.AdminMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理员鉴权辅助：复用现有 JwtUtil + admin 表，校验管理端请求身份。
 * 与用户登录共用同一套 JWT 机制，因此必须额外确认 token 中的 userId 是有效启用的管理员。
 */
@Component
public class AdminAuthUtil {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 从请求头解析当前管理员；无效或非管理员返回 null。
     */
    public Admin getCurrentAdmin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        Long adminId = jwtUtil.getUserIdFromToken(token);
        if (adminId == null) {
            return null;
        }
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null || admin.getStatus() == null || admin.getStatus() != 1) {
            return null;
        }
        return admin;
    }
}
