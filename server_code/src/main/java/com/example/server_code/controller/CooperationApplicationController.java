package com.example.server_code.controller;

import com.example.server_code.common.Result;
import com.example.server_code.dto.cooperation.CooperationApplicationRequest;
import com.example.server_code.service.CooperationApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * B端合作（公开端）：获取合作类型、提交合作申请。
 */
@RestController
@RequestMapping("/api/cooperations")
@CrossOrigin
public class CooperationApplicationController {

    @Autowired
    private CooperationApplicationService cooperationApplicationService;

    /**
     * 获取固定四种合作类型。
     */
    @GetMapping("/types")
    public Result<Map<String, String>> getTypes() {
        return Result.success(cooperationApplicationService.getTypes());
    }

    /**
     * 提交合作申请。status 由后端固定为 0，不允许前端传入。
     */
    @PostMapping("/applications")
    public Result<Void> submitApplication(@RequestBody CooperationApplicationRequest request) {
        cooperationApplicationService.submit(request);
        return Result.success();
    }
}
