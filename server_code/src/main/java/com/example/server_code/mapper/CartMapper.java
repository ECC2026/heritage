package com.example.server_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.server_code.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
