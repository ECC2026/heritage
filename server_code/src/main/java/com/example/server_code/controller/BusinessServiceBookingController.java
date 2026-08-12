package com.example.server_code.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.BusinessServiceBooking;
import com.example.server_code.entity.User;
import com.example.server_code.service.BusinessServiceBookingService;
import com.example.server_code.utils.UserAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务预约（用户侧）：我的预约、取消预约。
 */
@RestController
@RequestMapping("/api/service-bookings")
@CrossOrigin
public class BusinessServiceBookingController {

    @Autowired
    private BusinessServiceBookingService bookingService;

    @Autowired
    private UserAuthUtil userAuthUtil;

    /**
     * 我的预约（需登录）。
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyBookings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        User user = userAuthUtil.getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        Page<BusinessServiceBooking> result = bookingService.getMyBookings(user.getId(), page, size);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    /**
     * 取消自己的预约（需登录）。
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelBooking(@PathVariable Long id, HttpServletRequest request) {
        User user = userAuthUtil.getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        bookingService.cancelBooking(user.getId(), id);
        return Result.success();
    }
}
