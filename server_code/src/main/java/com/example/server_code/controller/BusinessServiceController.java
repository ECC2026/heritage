package com.example.server_code.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.dto.service.ServiceBookingRequest;
import com.example.server_code.entity.BusinessService;
import com.example.server_code.entity.BusinessServiceSchedule;
import com.example.server_code.entity.User;
import com.example.server_code.service.BusinessServiceBookingService;
import com.example.server_code.service.BusinessServiceService;
import com.example.server_code.utils.UserAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 康养 / 演艺等服务公开接口。
 */
@RestController
@RequestMapping("/api/services")
@CrossOrigin
public class BusinessServiceController {

    @Autowired
    private BusinessServiceService businessServiceService;

    @Autowired
    private BusinessServiceBookingService bookingService;

    @Autowired
    private UserAuthUtil userAuthUtil;

    /**
     * 服务列表（启用）。支持 productSystemId、keyword、page、size。
     */
    @GetMapping
    public Result<Map<String, Object>> listServices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long productSystemId,
            @RequestParam(required = false) String keyword) {
        Page<BusinessService> result = businessServiceService.listEnabled(page, size, productSystemId, keyword);
        return Result.success(pagination(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 服务详情：基础信息 + productSystem + 可用场次。
     */
    @GetMapping("/{id}")
    public Result<BusinessService> getServiceDetail(@PathVariable Long id) {
        BusinessService service = businessServiceService.getDetail(id);
        if (service == null) {
            return Result.error("服务不存在");
        }
        return Result.success(service);
    }

    /**
     * 服务可用场次（启用且未结束）。
     */
    @GetMapping("/{id}/schedules")
    public Result<List<BusinessServiceSchedule>> getServiceSchedules(@PathVariable Long id) {
        if (businessServiceService.getDetail(id) == null) {
            return Result.error("服务不存在");
        }
        return Result.success(businessServiceService.listValidSchedules(id));
    }

    /**
     * 预约服务（需登录）。userId 一律来自登录态，不信赖前端。
     */
    @PostMapping("/{id}/bookings")
    public Result<Void> createBooking(@PathVariable Long id,
                                      @RequestBody ServiceBookingRequest request,
                                      HttpServletRequest httpRequest) {
        User user = userAuthUtil.getCurrentUser(httpRequest);
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        bookingService.createBooking(user.getId(), id, request);
        return Result.success();
    }

    private Map<String, Object> pagination(List<?> list, long total, long page, long size) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return data;
    }
}
