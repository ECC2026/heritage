package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.Activity;
import com.example.server_code.mapper.ActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin
public class ActivityController {
    
    @Autowired
    private ActivityMapper activityMapper;
    
    /**
     * 分页查询活动列表（管理端）。支持按名称模糊搜索和状态筛选，按开始时间降序排列。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param name 活动名称（模糊搜索）
     * @param status 状态筛选：0-待审核, 1-进行中, 2-已结束
     * @return { list, total, page, size }
     */
    @GetMapping
    public Result<Map<String, Object>> getActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        
        Page<Activity> pageParam = new Page<>(page, size);
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("start_time");
        
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        Page<Activity> result = activityMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::normalizeActivity);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }

    /**
     * 查询所有进行中（status=1）的活动，按开始时间升序，用于小程序客户端活动列表页。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return { list, total, page, size }
     */
    @GetMapping("/enable")
    public Result<Map<String, Object>> getEnableActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Activity> pageParam = new Page<>(page, size);
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("start_time");

        Page<Activity> result = activityMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::normalizeActivity);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }
    
    /**
     * 获取单个活动详情。
     * @param id 活动ID
     * @return 活动对象
     */
    @GetMapping("/{id}")
    public Result<Activity> getActivityById(@PathVariable Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        normalizeActivity(activity);
        return Result.success(activity);
    }
    
    /**
     * 新增活动。自动将organizer复制到organizerName备用字段。
     * @param activity 活动JSON对象
     * @return 成功无数据返回
     */
    @PostMapping
    public Result<Void> addActivity(@RequestBody Activity activity) {
        if (activity.getOrganizerName() == null && activity.getOrganizer() != null) {
            activity.setOrganizerName(activity.getOrganizer());
        }
        activityMapper.insert(activity);
        return Result.success();
    }
    
    /**
     * 修改指定活动。
     * @param id 活动ID
     * @param activity 更新的活动JSON对象
     * @return 成功无数据返回
     */
    @PutMapping("/{id}")
    public Result<Void> updateActivity(@PathVariable Long id, @RequestBody Activity activity) {
        activity.setId(id);
        if (activity.getOrganizerName() == null && activity.getOrganizer() != null) {
            activity.setOrganizerName(activity.getOrganizer());
        }
        activityMapper.updateById(activity);
        return Result.success();
    }
    
    /**
     * 删除指定活动。
     * @param id 活动ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        activityMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 修改活动状态。
     * @param id 活动ID
     * @param params { status: 0-待审核, 1-进行中, 2-已结束 }
     * @return 成功无数据返回
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateActivityStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        activity.setStatus(params.get("status"));
        activityMapper.updateById(activity);
        return Result.success();
    }
    
    /**
     * 审核活动（与status接口功能相同，语义上专用于审核操作）。
     * @param id 活动ID
     * @param params { status: 0-待审核, 1-进行中, 2-已结束 }
     * @return 成功无数据返回
     */
    @PutMapping("/{id}/audit")
    public Result<Void> auditActivity(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        activity.setStatus(params.get("status"));
        activityMapper.updateById(activity);
        return Result.success();
    }

    private void normalizeActivity(Activity activity) {
        activity.setTitle(activity.getName());
        activity.setMaxParticipants(activity.getLimitCount());
        activity.setOrganizer(activity.getOrganizerName());
        Integer status = activity.getStatus();
        if (status == null) {
            activity.setStatusText("未知");
            return;
        }
        activity.setStatusText(switch (status) {
            case 0 -> "待审核";
            case 1 -> "进行中";
            case 2 -> "已结束";
            default -> "未知";
        });
    }
}
