package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.server_code.common.Result;
import com.example.server_code.entity.Admin;
import com.example.server_code.mapper.AdminMapper;
import com.example.server_code.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {
    
    @Autowired
    private AdminMapper adminMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 管理员登录。验证用户名+MD5密码，返回JWT token和管理员基本信息（id、username、realName、avatar）。
     * @param params { username, password }
     * @return { token, admin: { id, username, realName, avatar } }
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String username = params.get("username");
        String password = params.get("password");
        
        if (username == null || password == null) {
            return Result.error("用户名或密码不能为空");
        }
        
        // 查询管理员
        QueryWrapper<Admin> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);//用querywrapper添加查询条件   查询admin记录
        Admin admin = adminMapper.selectOne(wrapper);
        
        if (admin == null) {
            return Result.error("用户不存在");
        }
        
        if (admin.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        
        // 验证密码
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(admin.getPassword())) {
            return Result.error("密码错误");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(admin.getUsername(), admin.getId());
        
        // 更新登录信息
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(request.getRemoteAddr());
        adminMapper.updateById(admin);
        
        // 返回结果
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", admin.getId());
        adminInfo.put("username", admin.getUsername());
        adminInfo.put("realName", admin.getRealName());
        adminInfo.put("avatar", admin.getAvatar());
        data.put("admin", adminInfo);
        
        return Result.success(data);
    }
    
    /**
     * 获取当前登录管理员的信息（需携带Authorization请求头）。
     * @return { id, username, realName, avatar }
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getAdminInfo(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            Long adminId = jwtUtil.getUserIdFromToken(token);
            Admin admin = adminMapper.selectById(adminId);
            
            if (admin == null) {
                return Result.error("用户不存在");
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", admin.getId());
            data.put("username", admin.getUsername());
            data.put("realName", admin.getRealName());
            data.put("avatar", admin.getAvatar());
            
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(401, "Token无效");
        }
    }
    
    /**
     * 管理员退出登录。前端清除token即可，后端仅返回成功。
     * @return 成功无数据返回
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
