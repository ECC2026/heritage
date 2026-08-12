package com.example.server_code.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.CooperationApplication;
import com.example.server_code.service.CooperationApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端：B端合作申请（列表 / 详情 / 修改状态 / 填写备注）。
 */
@RestController
@RequestMapping("/api/admin/cooperation-applications")
@CrossOrigin
public class AdminCooperationApplicationController extends AdminBaseController {

    @Autowired
    private CooperationApplicationService cooperationApplicationService;

    @GetMapping
    public Result<Map<String, Object>> pageApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        requireAdmin(request);
        Page<CooperationApplication> result =
                cooperationApplicationService.pageAll(page, size, companyName, status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<CooperationApplication> getApplicationDetail(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        return Result.success(cooperationApplicationService.getDetail(id));
    }

    /**
     * 修改状态并填写备注。body: { status, remark }。
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateApplicationStatus(@PathVariable Long id, @RequestBody Map<String, Object> params,
                                                HttpServletRequest request) {
        requireAdmin(request);
        Integer status = params.get("status") == null ? null : Integer.parseInt(String.valueOf(params.get("status")));
        String remark = params.get("remark") == null ? null : String.valueOf(params.get("remark"));
        cooperationApplicationService.updateStatus(id, status, remark);
        return Result.success();
    }
}
