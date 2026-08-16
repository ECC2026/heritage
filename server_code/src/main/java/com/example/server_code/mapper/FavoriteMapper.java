package com.example.server_code.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.server_code.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
