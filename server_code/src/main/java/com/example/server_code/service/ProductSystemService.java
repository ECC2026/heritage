package com.example.server_code.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.BizException;
import com.example.server_code.entity.ProductSystem;
import com.example.server_code.mapper.ProductSystemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 产品体系（新版商业维度），与旧 category 完全独立。
 */
@Service
public class ProductSystemService {

    @Autowired
    private ProductSystemMapper productSystemMapper;

    /**
     * 启用的产品体系列表，用于 C 端展示 / 下拉选项。
     */
    public List<ProductSystem> listEnabled() {
        QueryWrapper<ProductSystem> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("sort").orderByAsc("id");
        return productSystemMapper.selectList(wrapper);
    }

    /**
     * 管理端分页查询。
     */
    public Page<ProductSystem> pageAll(Integer page, Integer size, String name, Integer status) {
        Page<ProductSystem> pageParam = new Page<>(page, size);
        QueryWrapper<ProductSystem> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort").orderByAsc("id");
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        return productSystemMapper.selectPage(pageParam, wrapper);
    }

    public ProductSystem getById(Long id) {
        return productSystemMapper.selectById(id);
    }

    public void create(ProductSystem system) {
        validate(system);
        ProductSystem exists = selectByCode(system.getCode());
        if (exists != null) {
            throw new BizException("产品体系编码已存在");
        }
        if (system.getSort() == null) {
            system.setSort(0);
        }
        if (system.getStatus() == null) {
            system.setStatus(1);
        }
        productSystemMapper.insert(system);
    }

    public void update(ProductSystem system) {
        if (system.getId() == null || productSystemMapper.selectById(system.getId()) == null) {
            throw new BizException("产品体系不存在");
        }
        validate(system);
        if (system.getCode() != null) {
            ProductSystem exists = selectByCode(system.getCode());
            if (exists != null && !exists.getId().equals(system.getId())) {
                throw new BizException("产品体系编码已存在");
            }
        }
        productSystemMapper.updateById(system);
    }

    public void updateStatus(Long id, Integer status) {
        ProductSystem system = productSystemMapper.selectById(id);
        if (system == null) {
            throw new BizException("产品体系不存在");
        }
        system.setStatus(status);
        productSystemMapper.updateById(system);
    }

    private void validate(ProductSystem system) {
        if (!StringUtils.hasText(system.getName())) {
            throw new BizException("产品体系名称不能为空");
        }
        if (!StringUtils.hasText(system.getCode())) {
            throw new BizException("产品体系编码不能为空");
        }
    }

    private ProductSystem selectByCode(String code) {
        QueryWrapper<ProductSystem> wrapper = new QueryWrapper<>();
        wrapper.eq("code", code);
        return productSystemMapper.selectOne(wrapper);
    }
}
