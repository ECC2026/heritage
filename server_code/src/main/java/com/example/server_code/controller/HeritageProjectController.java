package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.HeritageProject;
import com.example.server_code.mapper.HeritageProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/heritage-projects")
@CrossOrigin
public class HeritageProjectController {
    
    @Autowired
    private HeritageProjectMapper heritageProjectMapper;
    
    /**
     * 分页查询非遗项目列表。支持按名称模糊搜索、按状态筛选，按sort字段降序排列。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param name 项目名称（模糊搜索）
     * @param status 状态筛选
     * @return { list, total, page, size }
     */
    @GetMapping
    public Result<Map<String, Object>> getProjects(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        
        Page<HeritageProject> pageParam = new Page<>(page, size);
        QueryWrapper<HeritageProject> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("sort");
        
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        Page<HeritageProject> result = heritageProjectMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }
    
    /**
     * 获取所有启用状态（status=1）的非遗项目，用于小程序首页展示。
     * @return 非遗项目列表
     */
    @GetMapping("/all")
    public Result<List<HeritageProject>> getAllProjects() {
        QueryWrapper<HeritageProject> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("sort");
        List<HeritageProject> list = heritageProjectMapper.selectList(wrapper);
        return Result.success(list);
    }
    
    /**
     * 根据ID获取单个非遗项目的详细信息。
     * @param id 项目ID
     * @return 非遗项目对象
     */
    @GetMapping("/{id}")
    public Result<HeritageProject> getProjectById(@PathVariable Long id) {
        HeritageProject project = heritageProjectMapper.selectById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        return Result.success(project);
    }
    
    /**
     * 新增一个非遗项目。
     * @param project 非遗项目JSON对象
     * @return 成功无数据返回
     */
    @PostMapping
    public Result<Void> addProject(@RequestBody HeritageProject project) {
        heritageProjectMapper.insert(project);
        return Result.success();
    }
    
    /**
     * 修改指定非遗项目的信息。
     * @param id 项目ID
     * @param project 更新的项目JSON对象
     * @return 成功无数据返回
     */
    @PutMapping("/{id}")
    public Result<Void> updateProject(@PathVariable Long id, @RequestBody HeritageProject project) {
        project.setId(id);
        heritageProjectMapper.updateById(project);
        return Result.success();
    }
    
    /**
     * 删除指定非遗项目。
     * @param id 项目ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        heritageProjectMapper.deleteById(id);
        return Result.success();
    }
}
