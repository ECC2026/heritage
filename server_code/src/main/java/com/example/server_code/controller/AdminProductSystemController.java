package com.example.server_code.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.ProductSystem;
import com.example.server_code.service.ProductSystemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端：产品体系管理（列表 / 新增 / 编辑 / 启用禁用）。
 */
@RestController
@RequestMapping("/api/admin/product-systems")
@CrossOrigin
public class AdminProductSystemController extends AdminBaseController {

    @Autowired
    private ProductSystemService productSystemService;

    @GetMapping
    public Result<Map<String, Object>> pageSystems(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        requireAdmin(request);
        Page<ProductSystem> result = productSystemService.pageAll(page, size, name, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @PostMapping
    public Result<Void> createSystem(@RequestBody ProductSystem system, HttpServletRequest request) {
        requireAdmin(request);
        productSystemService.create(system);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateSystem(@PathVariable Long id, @RequestBody ProductSystem system, HttpServletRequest request) {
        requireAdmin(request);
        system.setId(id);
        productSystemService.update(system);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateSystemStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params,
                                           HttpServletRequest request) {
        requireAdmin(request);
        productSystemService.updateStatus(id, params.get("status"));
        return Result.success();
    }
}
