package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.Inheritor;
import com.example.server_code.entity.User;
import com.example.server_code.mapper.InheritorMapper;
import com.example.server_code.mapper.UserMapper;
import com.example.server_code.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/inheritors")
@CrossOrigin
public class InheritorController {
    
    @Autowired
    private InheritorMapper inheritorMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 分页查询传承人列表（管理端）。支持按姓名模糊搜索和审核状态筛选。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param name 姓名（模糊搜索）
     * @param auditStatus 审核状态筛选：0-待审核, 1-已通过, 2-已拒绝
     * @return { list, total, page, size }
     */
    @GetMapping
    public Result<Map<String, Object>> getInheritors(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer auditStatus) {
        
        Page<Inheritor> pageParam = new Page<>(page, size);
        QueryWrapper<Inheritor> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (auditStatus != null) {
            wrapper.eq("audit_status", auditStatus);
        }
        
        Page<Inheritor> result = inheritorMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }
    
    /**
     * 获取单个传承人详情。
     * @param id 传承人ID
     * @return 传承人对象
     */
    @GetMapping("/{id}")
    public Result<Inheritor> getInheritorById(@PathVariable Long id) {
        Inheritor inheritor = inheritorMapper.selectById(id);
        if (inheritor == null) {
            return Result.error("传承人不存在");
        }
        return Result.success(inheritor);
    }

    /**
     * 当前登录用户查看自己的传承人认证申请（返回最新一条记录）。
     * @return 传承人申请对象或null
     */
    @GetMapping("/my")
    public Result<Inheritor> getMyApplication(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(findLatestByUserId(user.getId()));
    }

    /**
     * 提交传承人认证申请（需登录）。必填姓名、电话、技艺类型、技艺简介。已提交待审核或已通过时不允许重复提交，已拒绝可重新提交。
     * @param params { name, phone, skillType, skillDesc, idCard?, experience?, certificate? }
     * @return 申请记录对象
     */
    @PostMapping("/apply")
    public Result<Inheritor> apply(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        String name = trim(params.get("name"));
        String phone = trim(params.get("phone"));
        String skillType = trim(params.get("skillType"));
        String skillDesc = trim(params.get("skillDesc"));

        if (name == null || phone == null || skillType == null || skillDesc == null) {
            return Result.error("请完善姓名、联系电话、技艺类型和技艺简介");
        }

        Inheritor current = findLatestByUserId(user.getId());
        if (current != null) {
            Integer auditStatus = current.getAuditStatus();
            if (auditStatus != null && auditStatus == 0) {
                return Result.error("你已提交申请，请等待审核");
            }
            if (auditStatus != null && auditStatus == 1) {
                return Result.error("你已通过传承人认证");
            }
        }

        Inheritor inheritor = current != null ? current : new Inheritor();
        LocalDateTime now = LocalDateTime.now();
        inheritor.setUserId(user.getId());
        inheritor.setName(name);
        inheritor.setPhone(phone);
        inheritor.setIdCard(trim(params.get("idCard")));
        inheritor.setSkillType(skillType);
        inheritor.setSkillDesc(skillDesc);
        inheritor.setExperience(trim(params.get("experience")));
        inheritor.setCertificate(trim(params.get("certificate")));
        inheritor.setAuditStatus(0);
        inheritor.setAuditRemark(null);
        inheritor.setAuditTime(null);
        inheritor.setStatus(1);
        inheritor.setUpdateTime(now);

        if (current == null) {
            inheritor.setCreateTime(now);
            inheritorMapper.insert(inheritor);
        } else {
            inheritorMapper.updateById(inheritor);
        }

        return Result.success("申请已提交", inheritor);
    }
    
    /**
     * 管理员审核传承人申请，记录审核时间。
     * @param id 传承人申请ID
     * @param params { auditStatus: 0-待审核, 1-已通过, 2-已拒绝 }
     * @return 成功无数据返回
     */
    @PutMapping("/{id}/audit")
    public Result<Void> auditInheritor(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Inheritor inheritor = inheritorMapper.selectById(id);
        if (inheritor == null) {
            return Result.error("传承人不存在");
        }
        inheritor.setAuditStatus(params.get("auditStatus"));
        inheritor.setAuditTime(LocalDateTime.now());
        inheritorMapper.updateById(inheritor);
        return Result.success();
    }

    private Inheritor findLatestByUserId(Long userId) {
        QueryWrapper<Inheritor> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("update_time")
                .orderByDesc("create_time")
                .last("LIMIT 1");
        return inheritorMapper.selectOne(wrapper, false);
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

    private String trim(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
