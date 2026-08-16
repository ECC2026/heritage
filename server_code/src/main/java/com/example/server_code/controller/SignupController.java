package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.Activity;
import com.example.server_code.entity.Signup;
import com.example.server_code.entity.User;
import com.example.server_code.mapper.ActivityMapper;
import com.example.server_code.mapper.SignupMapper;
import com.example.server_code.mapper.UserMapper;
import com.example.server_code.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/signups")
@CrossOrigin
public class SignupController {

    @Autowired
    private SignupMapper signupMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 分页查询所有报名记录（管理端）。支持按活动名称模糊搜索和状态筛选，自动关联活动信息（名称、封面、地点、时间）。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param activityName 活动名称（模糊搜索）
     * @param status 状态筛选：0-待审核, 1-已通过, 2-已拒绝, 3-已取消
     * @return { list, total, page, size }
     */
    @GetMapping
    public Result<Map<String, Object>> getSignups(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String activityName,
            @RequestParam(required = false) Integer status) {
        List<Signup> filtered = querySignups(activityName, status);
        int fromIndex = Math.max((page - 1) * size, 0);
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<Signup> pageList = fromIndex >= filtered.size() ? List.of() : filtered.subList(fromIndex, toIndex);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageList);
        data.put("total", filtered.size());
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /**
     * 导出报名记录为CSV文件。含BOM头兼容Excel打开，列包含：ID、活动名称、报名人、联系电话、状态、报名时间、备注。
     * @param activityName 活动名称（模糊搜索，可选）
     * @param status 状态筛选（可选）
     * @return CSV文件下载
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportSignups(
            @RequestParam(required = false) String activityName,
            @RequestParam(required = false) Integer status) {
        List<Signup> signups = querySignups(activityName, status);
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("ID,活动名称,报名人,联系电话,状态,报名时间,备注\n");
        for (Signup signup : signups) {
            csv.append(signup.getId()).append(",")
                    .append(escapeCsv(signup.getActivityName())).append(",")
                    .append(escapeCsv(signup.getUserName())).append(",")
                    .append(escapeCsv(signup.getPhone())).append(",")
                    .append(escapeCsv(getStatusText(signup.getStatus()))).append(",")
                    .append(escapeCsv(String.valueOf(signup.getCreateTime()))).append(",")
                    .append(escapeCsv(signup.getRemark()))
                    .append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=signups.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取单条报名记录详情（含关联的活动信息）。
     * @param id 报名ID
     * @return 报名对象
     */
    @GetMapping("/{id}")
    public Result<Signup> getSignupDetail(@PathVariable Long id) {
        Signup signup = signupMapper.selectById(id);
        if (signup == null) {
            return Result.error("报名记录不存在");
        }
        fillSignupActivity(signup);
        return Result.success(signup);
    }

    /**
     * 当前登录用户查看自己的报名记录（含关联的活动信息）。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return { list, total, page, size }
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> getMySignups(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Page<Signup> pageParam = new Page<>(page, size);
        QueryWrapper<Signup> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId()).orderByDesc("create_time");
        Page<Signup> result = signupMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillSignupActivity);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    /**
     * 用户报名活动（需登录）。校验活动是否可报名、名额是否已满、是否重复报名。已取消的记录可重新报名。
     * @param params { activityId, remark? }
     * @return 成功无数据返回
     */
    @PostMapping
    public Result<Void> createSignup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Long activityId = toLong(params.get("activityId"));
        if (activityId == null) {
            return Result.error("活动不存在");
        }

        Activity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() == null || activity.getStatus() != 1) {
            return Result.error("活动不可报名");
        }
        if (activity.getLimitCount() != null && activity.getLimitCount() > 0) {
            QueryWrapper<Signup> countWrapper = new QueryWrapper<>();
            countWrapper.eq("activity_id", activityId).in("status", 0, 1);
            Long currentCount = signupMapper.selectCount(countWrapper);
            if (currentCount >= activity.getLimitCount()) {
                return Result.error("活动报名人数已满");
            }
        }

        QueryWrapper<Signup> existsWrapper = new QueryWrapper<>();
        existsWrapper.eq("activity_id", activityId).eq("user_id", user.getId());
        Signup exists = signupMapper.selectOne(existsWrapper);
        if (exists != null && exists.getStatus() != null && exists.getStatus() != 3) {
            return Result.error("您已报名该活动");
        }

        Signup signup = exists == null ? new Signup() : exists;
        signup.setActivityId(activityId);
        signup.setUserId(user.getId());
        signup.setUserName(displayName(user));
        signup.setPhone(user.getPhone());
        signup.setRemark(params.get("remark") == null ? null : String.valueOf(params.get("remark")));
        signup.setStatus(0);

        if (exists == null) {
            signupMapper.insert(signup);
        } else {
            signupMapper.updateById(signup);
        }
        refreshSignupCount(activityId);
        return Result.success();
    }

    /**
     * 审核报名记录。记录审核时间并刷新活动报名人数。
     * @param id 报名ID
     * @param params { status: 0-待审核, 1-已通过, 2-已拒绝 }
     * @return 成功无数据返回
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateSignupStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Signup signup = signupMapper.selectById(id);
        if (signup == null) {
            return Result.error("报名记录不存在");
        }

        signup.setStatus(Integer.parseInt(String.valueOf(params.get("status"))));
        signup.setAuditTime(LocalDateTime.now());
        signupMapper.updateById(signup);
        refreshSignupCount(signup.getActivityId());
        return Result.success();
    }

    /**
     * 用户取消自己的报名（需登录，仅允许取消本人的报名）。
     * @param id 报名ID
     * @return 成功无数据返回
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelSignup(@PathVariable Long id, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Signup signup = signupMapper.selectById(id);
        if (signup == null) {
            return Result.error("报名记录不存在");
        }
        if (!user.getId().equals(signup.getUserId())) {
            return Result.error(403, "无权取消该报名");
        }
        if (signup.getStatus() != null && signup.getStatus() == 3) {
            return Result.success();
        }

        signup.setStatus(3);
        signup.setCancelTime(LocalDateTime.now());
        signupMapper.updateById(signup);
        refreshSignupCount(signup.getActivityId());
        return Result.success();
    }

    private void refreshSignupCount(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            return;
        }
        QueryWrapper<Signup> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId).in("status", 0, 1);
        Long count = signupMapper.selectCount(wrapper);
        activity.setSignupCount(count.intValue());
        activityMapper.updateById(activity);
    }

    private List<Signup> querySignups(String activityName, Integer status) {
        QueryWrapper<Signup> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");

        List<Signup> all = signupMapper.selectList(wrapper);
        List<Signup> filtered = new ArrayList<>();
        for (Signup signup : all) {
            fillSignupActivity(signup);
            if (activityName == null || activityName.isBlank()
                    || (signup.getActivityName() != null && signup.getActivityName().contains(activityName))) {
                filtered.add(signup);
            }
        }
        return filtered;
    }

    private void fillSignupActivity(Signup signup) {
        Activity activity = activityMapper.selectById(signup.getActivityId());
        if (activity == null) {
            return;
        }
        signup.setActivityName(activity.getName());
        signup.setActivityCover(activity.getCover());
        signup.setActivityLocation(activity.getLocation());
        signup.setActivityStartTime(activity.getStartTime());
        signup.setActivityEndTime(activity.getEndTime());
    }

    private String getStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
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

    private String displayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return "用户" + user.getId();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(value));
    }
}
