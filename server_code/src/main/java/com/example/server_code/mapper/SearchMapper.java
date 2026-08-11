package com.example.server_code.mapper;

import com.example.server_code.dto.search.SearchItemDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SearchMapper {
    long countSearchResults(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("categoryId") Long categoryId,
            @Param("levelCode") String levelCode,
            @Param("regionCode") String regionCode);

    List<SearchItemDto> selectSearchResults(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("categoryId") Long categoryId,
            @Param("levelCode") String levelCode,
            @Param("regionCode") String regionCode,
            @Param("offset") long offset,
            @Param("size") int size);
}
