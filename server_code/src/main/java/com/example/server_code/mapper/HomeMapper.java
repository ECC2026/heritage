package com.example.server_code.mapper;

import com.example.server_code.dto.home.ActivitySummaryDto;
import com.example.server_code.dto.home.BannerDto;
import com.example.server_code.dto.home.CategoryDto;
import com.example.server_code.dto.home.CityDto;
import com.example.server_code.dto.home.CourseSummaryDto;
import com.example.server_code.dto.home.HeritageLevelDto;
import com.example.server_code.dto.home.HeritageProjectSummaryDto;
import com.example.server_code.dto.home.InheritorSummaryDto;
import com.example.server_code.dto.home.NewsSummaryDto;
import com.example.server_code.dto.home.ProductSummaryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HomeMapper {
    List<CityDto> selectActiveCities();

    CityDto selectCityByCode(@Param("code") String code);

    CityDto selectDefaultCity();

    List<CategoryDto> selectHeritageCategories();

    List<HeritageLevelDto> selectHeritageLevels();

    List<BannerDto> selectEnabledBanners(@Param("limit") int limit);

    List<HeritageProjectSummaryDto> selectRecommendedProjects(
            @Param("limit") int limit,
            @Param("regionCode") String regionCode);

    List<InheritorSummaryDto> selectRecommendedInheritors(@Param("limit") int limit);

    List<ProductSummaryDto> selectRecommendedProducts(@Param("limit") int limit);

    List<CourseSummaryDto> selectWeeklyCourses(
            @Param("cityCode") String cityCode,
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd,
            @Param("limit") int limit);

    List<ActivitySummaryDto> selectRecommendedActivities(
            @Param("cityCode") String cityCode,
            @Param("now") LocalDateTime now,
            @Param("limit") int limit);

    List<NewsSummaryDto> selectHomeNews(@Param("limit") int limit);
}
