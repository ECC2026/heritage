package com.example.server_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.server_code.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
