package com.example.server_code.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.BusinessService;
import com.example.server_code.entity.BusinessServiceSchedule;
import com.example.server_code.service.BusinessServiceService;
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
import java.util.List;
import java.util.Map;

/**
 * 管理端：服务管理（列表 / 新增 / 编辑 / 启用禁用 / 场次查看与新增）。
 */
@RestController
@RequestMapping("/api/admin/services")
@CrossOrigin
public class AdminBusinessServiceController extends AdminBaseController {

    @Autowired
    private BusinessServiceService businessServiceService;

    @GetMapping
    public Result<Map<String, Object>> pageServices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long productSystemId,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        requireAdmin(request);
        Page<BusinessService> result = businessServiceService.pageAll(page, size, name, productSystemId, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @PostMapping
    public Result<Void> createService(@RequestBody BusinessService service, HttpServletRequest request) {
        requireAdmin(request);
        businessServiceService.create(service);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateService(@PathVariable Long id, @RequestBody BusinessService service,
                                      HttpServletRequest request) {
        requireAdmin(request);
        service.setId(id);
        businessServiceService.update(service);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateServiceStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params,
                                            HttpServletRequest request) {
        requireAdmin(request);
        businessServiceService.updateStatus(id, params.get("status"));
        return Result.success();
    }

    @GetMapping("/{id}/schedules")
    public Result<List<BusinessServiceSchedule>> listSchedules(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        return Result.success(businessServiceService.listSchedules(id));
    }

    @PostMapping("/{id}/schedules")
    public Result<Void> addSchedule(@PathVariable Long id, @RequestBody BusinessServiceSchedule schedule,
                                    HttpServletRequest request) {
        requireAdmin(request);
        businessServiceService.addSchedule(id, schedule);
        return Result.success();
    }
}
