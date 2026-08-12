package com.example.server_code.utils;

import com.example.server_code.entity.User;
import com.example.server_code.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * C 端用户鉴权辅助：从 Authorization 请求头解析当前登录用户。
 * 用户身份一律来自 token，绝不信赖前端传入的 userId。
 */
@Component
public class UserAuthUtil {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    /**
     * 从请求头解析当前用户；未登录或 token 无效返回 null。
     */
    public User getCurrentUser(HttpServletRequest request) {
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
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return null;
        }
        return userMapper.selectById(userId);
    }
}
