package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.Banner;
import com.example.server_code.mapper.BannerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
@CrossOrigin
public class BannerController {
    
    @Autowired
    private BannerMapper bannerMapper;
    
    /**
     * 分页查询轮播图列表（管理端）。支持按标题模糊搜索和状态筛选，按sort字段降序排列。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param title 标题（模糊搜索）
     * @param status 状态筛选
     * @return { list, total, page, size }
     */
    @GetMapping
    public Result<Map<String, Object>> getBanners(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        
        Page<Banner> pageParam = new Page<>(page, size);
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("sort");
        
        if (title != null && !title.isEmpty()) {
            wrapper.like("title", title);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        Page<Banner> result = bannerMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }

    /**
     * 获取所有启用（status=1）的轮播图，按sort升序排列，用于小程序客户端首页轮播展示。
     * @return 轮播图列表
     */
    @GetMapping("/enable")
    public Result<List<Banner>> getEnableBanners() {
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("sort");
        return Result.success(bannerMapper.selectList(wrapper));
    }
    
    /**
     * 获取单个轮播图详情。
     * @param id 轮播图ID
     * @return 轮播图对象
     */
    @GetMapping("/{id}")
    public Result<Banner> getBannerById(@PathVariable Long id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }
        return Result.success(banner);
    }
    
    /**
     * 新增轮播图。
     * @param banner { title, image, link, linkType, sort, status }
     * @return 成功无数据返回
     */
    @PostMapping
    public Result<Void> addBanner(@RequestBody Banner banner) {
        bannerMapper.insert(banner);
        return Result.success();
    }
    
    /**
     * 修改指定轮播图。
     * @param id 轮播图ID
     * @param banner 更新的轮播图JSON对象
     * @return 成功无数据返回
     */
    @PutMapping("/{id}")
    public Result<Void> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerMapper.updateById(banner);
        return Result.success();
    }
    
    /**
     * 删除指定轮播图。
     * @param id 轮播图ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        bannerMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 启用/禁用轮播图。
     * @param id 轮播图ID
     * @param params { status: 1-启用, 0-禁用 }
     * @return 成功无数据返回
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateBannerStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }
        banner.setStatus(params.get("status"));
        bannerMapper.updateById(banner);
        return Result.success();
    }
}
