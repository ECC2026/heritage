package com.example.server_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.BizException;
import com.example.server_code.dto.cooperation.CooperationApplicationRequest;
import com.example.server_code.entity.CooperationApplication;
import com.example.server_code.mapper.CooperationApplicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * B端合作申请。合作类型为固定四种，不引入复杂 CMS。
 */
@Service
public class CooperationApplicationService {

    /** 固定合作类型：code -> 展示名称。放在 Service 而非硬编码进 Controller。 */
    private static final Map<String, String> COOPERATION_TYPES = new LinkedHashMap<>();

    static {
        COOPERATION_TYPES.put("tourism_cooperation", "文旅合作");
        COOPERATION_TYPES.put("corporate_customization", "企业定制");
        COOPERATION_TYPES.put("heritage_event", "非遗活动落地");
        COOPERATION_TYPES.put("platform_join", "平台入驻");
    }

    @Autowired
    private CooperationApplicationMapper applicationMapper;

    /**
     * 获取固定四种合作类型。
     */
    public Map<String, String> getTypes() {
        return COOPERATION_TYPES;
    }

    /**
     * 提交合作申请。status 后端固定为 0（待处理），不允许前端传入。
     */
    public void submit(CooperationApplicationRequest request) {
        if (request == null) {
            throw new BizException("申请内容不能为空");
        }
        if (!StringUtils.hasText(request.getCompanyName())) {
            throw new BizException("企业/机构名称不能为空");
        }
        if (!StringUtils.hasText(request.getContactName())) {
            throw new BizException("联系人不能为空");
        }
        if (!StringUtils.hasText(request.getContactPhone())) {
            throw new BizException("联系电话不能为空");
        }
        if (!COOPERATION_TYPES.containsKey(request.getCooperationType())) {
            throw new BizException("合作类型不合法");
        }

        CooperationApplication application = new CooperationApplication();
        application.setCompanyName(request.getCompanyName().trim());
        application.setContactName(request.getContactName().trim());
        application.setContactPhone(request.getContactPhone().trim());
        application.setCooperationType(request.getCooperationType());
        application.setRequirement(StringUtils.hasText(request.getRequirement())
                ? request.getRequirement().trim() : null);
        application.setStatus(0);
        applicationMapper.insert(application);
    }

    // ==================== 管理端 ====================

    public Page<CooperationApplication> pageAll(Integer page, Integer size, String companyName, Integer status) {
        Page<CooperationApplication> pageParam = new Page<>(page, size);
        QueryWrapper<CooperationApplication> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (StringUtils.hasText(companyName)) {
            wrapper.like("company_name", companyName);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        Page<CooperationApplication> result = applicationMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::fillText);
        return result;
    }

    public CooperationApplication getDetail(Long id) {
        CooperationApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BizException("合作申请不存在");
        }
        fillText(application);
        return application;
    }

    /**
     * 管理端修改状态并填写备注。
     */
    public void updateStatus(Long id, Integer status, String remark) {
        CooperationApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BizException("合作申请不存在");
        }
        if (status != null && (status < 0 || status > 3)) {
            throw new BizException("状态不合法");
        }
        application.setStatus(status == null ? application.getStatus() : status);
        application.setRemark(StringUtils.hasText(remark) ? remark.trim() : null);
        applicationMapper.updateById(application);
    }

    public static String typeText(String code) {
        if (code == null) {
            return null;
        }
        return COOPERATION_TYPES.getOrDefault(code, code);
    }

    public static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "已联系";
            case 2 -> "已完成";
            case 3 -> "已关闭";
            default -> "未知";
        };
    }

    private void fillText(CooperationApplication application) {
        application.setCooperationTypeText(typeText(application.getCooperationType()));
        application.setStatusText(statusText(application.getStatus()));
    }
}
