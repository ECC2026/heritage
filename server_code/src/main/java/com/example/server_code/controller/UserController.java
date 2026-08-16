package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.server_code.common.Result;
import com.example.server_code.entity.User;
import com.example.server_code.mapper.UserMapper;
import com.example.server_code.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录。支持手机号或用户名+密码登录，成功返回JWT token，记录最后登录时间和IP。
     * @param params { phone, username, password } phone和username二选一
     * @return JWT token字符串
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String phone = trim(params.get("phone"));
        String username = trim(params.get("username"));
        String password = trim(params.get("password"));

        if ((phone == null && username == null) || password == null) {
            return Result.error("账号或密码不能为空");
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (phone != null) {
            wrapper.eq("phone", phone);
        } else {
            wrapper.eq("username", username);
        }

        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        if (!matchesPassword(password, user.getPassword())) {
            return Result.error("密码错误");
        }

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(request.getRemoteAddr());
        userMapper.updateById(user);

        return Result.success(jwtUtil.generateToken(user.getUsername(), user.getId()));
    }

    /**
     * 用户注册。必填手机号和密码，可选昵称和用户名。自动生成默认昵称（"用户"+手机号后4位）和默认头像。
     * @param params { phone, password, nickname?, username? }
     * @return 成功无数据返回
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> params) {
        String phone = trim(params.get("phone"));
        String password = trim(params.get("password"));
        String nickname = trim(params.get("nickname"));
        String username = trim(params.get("username"));

        if (phone == null || password == null) {
            return Result.error("手机号和密码不能为空");
        }

        QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
        phoneWrapper.eq("phone", phone);
        if (userMapper.selectCount(phoneWrapper) > 0) {
            return Result.error("手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setUsername(username != null ? username : phone);
        user.setPassword(md5(password));
        user.setNickname(nickname != null ? nickname : "用户" + phone.substring(Math.max(0, phone.length() - 4)));
        user.setAvatar("/static/img/logo.png");
        user.setGender(0);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return Result.success();
    }

    /**
     * 获取当前登录用户的个人信息（需携带Authorization请求头），返回不含password字段的用户对象。
     * @return 用户对象（不含password）
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改当前登录用户的个人信息。支持昵称、头像、邮箱、性别、生日，全部字段可选，传啥改啥。
     * @param params { nickname?, avatar?, email?, gender?, birthday? }
     * @return 更新后的用户对象
     */
    @PutMapping("/info")
    public Result<User> updateUserInfo(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        if (params.get("nickname") != null) {
            user.setNickname(String.valueOf(params.get("nickname")).trim());
        }
        if (params.get("avatar") != null) {
            user.setAvatar(String.valueOf(params.get("avatar")).trim());
        }
        if (params.get("email") != null) {
            user.setEmail(String.valueOf(params.get("email")).trim());
        }
        if (params.get("gender") != null) {
            user.setGender(Integer.parseInt(String.valueOf(params.get("gender"))));
        }
        if (params.get("birthday") != null) {
            user.setBirthday(LocalDate.parse(String.valueOf(params.get("birthday"))));
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        user.setPassword(null);
        return Result.success(user);
    }

    private User getCurrentUser(HttpServletRequest request) {
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
        return userMapper.selectById(userId);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        return storedPassword.equals(rawPassword) || storedPassword.equals(md5(rawPassword));
    }

    private String md5(String value) {
        return DigestUtils.md5DigestAsHex(value.getBytes());
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }
}
