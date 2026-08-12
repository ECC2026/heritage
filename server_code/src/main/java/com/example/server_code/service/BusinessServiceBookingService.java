package com.example.server_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.BizException;
import com.example.server_code.dto.service.ServiceBookingRequest;
import com.example.server_code.entity.BusinessService;
import com.example.server_code.entity.BusinessServiceBooking;
import com.example.server_code.entity.BusinessServiceSchedule;
import com.example.server_code.mapper.BusinessServiceBookingMapper;
import com.example.server_code.mapper.BusinessServiceMapper;
import com.example.server_code.mapper.BusinessServiceScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 服务预约闭环：创建、我的预约、取消。预约/取消必须使用事务。
 */
@Service
public class BusinessServiceBookingService {

    @Autowired
    private BusinessServiceMapper serviceMapper;

    @Autowired
    private BusinessServiceScheduleMapper scheduleMapper;

    @Autowired
    private BusinessServiceBookingMapper bookingMapper;

    /**
     * 创建预约（事务）。userId 一律来自登录态，绝不信赖前端传入。
     */
    @Transactional
    public BusinessServiceBooking createBooking(Long userId, Long serviceId, ServiceBookingRequest request) {
        if (request == null || request.getScheduleId() == null) {
            throw new BizException("请选择预约场次");
        }
        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        if (quantity <= 0) {
            throw new BizException("预约人数必须大于0");
        }

        BusinessService service = serviceMapper.selectById(serviceId);
        if (service == null || service.getStatus() == null || service.getStatus() != 1) {
            throw new BizException("服务不存在或已停用");
        }

        BusinessServiceSchedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null || !serviceId.equals(schedule.getServiceId())) {
            throw new BizException("预约场次不存在");
        }
        if (schedule.getStatus() == null || schedule.getStatus() != 1) {
            throw new BizException("该场次不可预约");
        }
        if (schedule.getEndTime() == null || !schedule.getEndTime().isAfter(LocalDateTime.now())) {
            throw new BizException("该场次已结束");
        }

        // 原子占用名额：仅在容量允许时更新，避免并发超卖（一期足够，无需复杂资源锁）。
        UpdateWrapper<BusinessServiceSchedule> occupy = new UpdateWrapper<>();
        occupy.eq("id", schedule.getId())
                .and(w -> w.le("capacity", 0).or().apply("booked_count + {0} <= capacity", quantity));
        occupy.setSql("booked_count = booked_count + " + quantity);
        int rows = scheduleMapper.update(null, occupy);
        if (rows == 0) {
            throw new BizException("该场次预约名额不足");
        }

        BusinessServiceBooking booking = new BusinessServiceBooking();
        booking.setServiceId(serviceId);
        booking.setScheduleId(schedule.getId());
        booking.setUserId(userId);
        booking.setQuantity(quantity);
        booking.setContactName(request.getContactName());
        booking.setContactPhone(request.getContactPhone());
        booking.setStatus(1);
        bookingMapper.insert(booking);
        return booking;
    }

    /**
     * 我的预约（分页），附带服务与场次信息。
     */
    public Page<BusinessServiceBooking> getMyBookings(Long userId, Integer page, Integer size) {
        Page<BusinessServiceBooking> pageParam = new Page<>(page, size);
        QueryWrapper<BusinessServiceBooking> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        Page<BusinessServiceBooking> result = bookingMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillDetail);
        return result;
    }

    /**
     * 取消自己的预约（事务），恢复 booked_count。
     */
    @Transactional
    public void cancelBooking(Long userId, Long bookingId) {
        BusinessServiceBooking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BizException("预约记录不存在");
        }
        if (!userId.equals(booking.getUserId())) {
            throw new BizException(403, "无权取消该预约");
        }
        if (booking.getStatus() == null || booking.getStatus() != 1) {
            throw new BizException("当前状态不可取消");
        }

        booking.setStatus(2);
        bookingMapper.updateById(booking);

        // 恢复名额，下限为 0，防止历史数据导致负数。
        UpdateWrapper<BusinessServiceSchedule> restore = new UpdateWrapper<>();
        restore.eq("id", booking.getScheduleId())
                .setSql("booked_count = GREATEST(booked_count - " + booking.getQuantity() + ", 0)");
        scheduleMapper.update(null, restore);
    }

    private void fillDetail(BusinessServiceBooking booking) {
        BusinessService service = serviceMapper.selectById(booking.getServiceId());
        if (service != null) {
            booking.setServiceName(service.getName());
            booking.setServiceCover(service.getCover());
            booking.setServiceProvider(service.getProviderName());
        }
        BusinessServiceSchedule schedule = scheduleMapper.selectById(booking.getScheduleId());
        if (schedule != null) {
            booking.setScheduleStartTime(schedule.getStartTime());
            booking.setScheduleEndTime(schedule.getEndTime());
        }
        Integer status = booking.getStatus();
        if (status != null) {
            booking.setStatusText(switch (status) {
                case 1 -> "已预约";
                case 2 -> "已取消";
                case 3 -> "已完成";
                default -> "未知";
            });
        }
    }
}
