package com.example.server_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.server_code.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
