package com.example.server_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.BizException;
import com.example.server_code.entity.BusinessService;
import com.example.server_code.entity.BusinessServiceSchedule;
import com.example.server_code.entity.ProductSystem;
import com.example.server_code.mapper.BusinessServiceMapper;
import com.example.server_code.mapper.BusinessServiceScheduleMapper;
import com.example.server_code.mapper.ProductSystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务服务（康养陪伴 / 民俗演艺等）。
 */
@Service
public class BusinessServiceService {

    @Autowired
    private BusinessServiceMapper serviceMapper;

    @Autowired
    private BusinessServiceScheduleMapper scheduleMapper;

    @Autowired
    private ProductSystemMapper productSystemMapper;

    /**
     * C 端：启用服务分页列表，支持按产品体系与关键字筛选。
     */
    public Page<BusinessService> listEnabled(Integer page, Integer size, Long productSystemId, String keyword) {
        Page<BusinessService> pageParam = new Page<>(page, size);
        QueryWrapper<BusinessService> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (productSystemId != null) {
            wrapper.eq("product_system_id", productSystemId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("name", keyword).or().like("summary", keyword));
        }
        wrapper.orderByAsc("sort").orderByAsc("id");
        Page<BusinessService> result = serviceMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillProductSystem);
        return result;
    }

    /**
     * 服务详情（基础信息 + productSystem + 可展示的有效场次）。
     */
    public BusinessService getDetail(Long id) {
        BusinessService service = serviceMapper.selectById(id);
        if (service == null) {
            return null;
        }
        fillProductSystem(service);
        service.setSchedules(listValidSchedules(id));
        return service;
    }

    /**
     * 可展示的有效场次：启用中且未结束，按开始时间升序。
     */
    public List<BusinessServiceSchedule> listValidSchedules(Long serviceId) {
        QueryWrapper<BusinessServiceSchedule> wrapper = new QueryWrapper<>();
        wrapper.eq("service_id", serviceId)
                .eq("status", 1)
                .gt("end_time", LocalDateTime.now())
                .orderByAsc("start_time");
        List<BusinessServiceSchedule> schedules = scheduleMapper.selectList(wrapper);
        schedules.forEach(this::fillRemaining);
        return schedules;
    }

    // ==================== 管理端 ====================

    public Page<BusinessService> pageAll(Integer page, Integer size, String name, Long productSystemId, Integer status) {
        Page<BusinessService> pageParam = new Page<>(page, size);
        QueryWrapper<BusinessService> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        if (productSystemId != null) {
            wrapper.eq("product_system_id", productSystemId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        Page<BusinessService> result = serviceMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillProductSystem);
        return result;
    }

    public void create(BusinessService service) {
        validate(service);
        if (service.getStatus() == null) {
            service.setStatus(1);
        }
        validateStatus(service.getStatus());
        if (service.getSort() == null) {
            service.setSort(0);
        }
        serviceMapper.insert(service);
    }

    public void update(BusinessService service) {
        if (service.getId() == null || serviceMapper.selectById(service.getId()) == null) {
            throw new BizException("服务不存在");
        }
        validate(service);
        validateStatus(service.getStatus());
        serviceMapper.updateById(service);
    }

    public void updateStatus(Long id, Integer status) {
        BusinessService service = serviceMapper.selectById(id);
        if (service == null) {
            throw new BizException("服务不存在");
        }
        validateStatus(status);
        service.setStatus(status);
        serviceMapper.updateById(service);
    }

    /**
     * 管理端查看某服务全部场次（含停用/已结束）。
     */
    public List<BusinessServiceSchedule> listSchedules(Long serviceId) {
        QueryWrapper<BusinessServiceSchedule> wrapper = new QueryWrapper<>();
        wrapper.eq("service_id", serviceId).orderByDesc("start_time");
        List<BusinessServiceSchedule> schedules = scheduleMapper.selectList(wrapper);
        schedules.forEach(this::fillRemaining);
        return schedules;
    }

    /**
     * 管理端新增基础场次。
     */
    public void addSchedule(Long serviceId, BusinessServiceSchedule schedule) {
        if (serviceMapper.selectById(serviceId) == null) {
            throw new BizException("服务不存在");
        }
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            throw new BizException("场次开始/结束时间不能为空");
        }
        if (!schedule.getEndTime().isAfter(schedule.getStartTime())) {
            throw new BizException("结束时间必须晚于开始时间");
        }
        schedule.setServiceId(serviceId);
        schedule.setId(null);
        if (schedule.getCapacity() == null) {
            schedule.setCapacity(0);
        }
        if (schedule.getCapacity() < 0) {
            throw new BizException("场次容量不能小于0");
        }
        if (schedule.getBookedCount() == null) {
            schedule.setBookedCount(0);
        }
        if (schedule.getStatus() == null) {
            schedule.setStatus(1);
        }
        scheduleMapper.insert(schedule);
    }

    private void validate(BusinessService service) {
        if (!StringUtils.hasText(service.getName())) {
            throw new BizException("服务名称不能为空");
        }
        if (service.getProductSystemId() == null) {
            throw new BizException("请选择产品体系");
        }
        if (productSystemMapper.selectById(service.getProductSystemId()) == null) {
            throw new BizException("产品体系不存在");
        }
    }

    /**
     * 服务状态仅允许 0-停用、1-启用，非法值直接抛业务异常。
     */
    private void validateStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BizException("服务状态不合法，仅支持 0-停用、1-启用");
        }
    }

    private void fillProductSystem(BusinessService service) {
        if (service.getProductSystemId() == null) {
            return;
        }
        ProductSystem system = productSystemMapper.selectById(service.getProductSystemId());
        if (system != null) {
            service.setProductSystem(system.getName());
        }
    }

    private void fillRemaining(BusinessServiceSchedule schedule) {
        int capacity = schedule.getCapacity() == null ? 0 : schedule.getCapacity();
        int booked = schedule.getBookedCount() == null ? 0 : schedule.getBookedCount();
        schedule.setRemaining(capacity <= 0 ? Integer.MAX_VALUE : Math.max(capacity - booked, 0));
    }
}
