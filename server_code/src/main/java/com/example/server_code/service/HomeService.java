package com.example.server_code.service;

import com.example.server_code.dto.home.CategoryDto;
import com.example.server_code.dto.home.CityDto;
import com.example.server_code.dto.home.HeritageLevelDto;
import com.example.server_code.dto.home.HeritageProjectSummaryDto;
import com.example.server_code.dto.home.HomeResponse;
import com.example.server_code.dto.home.NewsSummaryDto;
import com.example.server_code.mapper.HomeMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class HomeService {
    private static final ZoneId HOME_ZONE = ZoneId.of("Asia/Shanghai");
    private static final int BANNER_LIMIT = 5;
    private static final int PROJECT_LIMIT = 6;
    private static final int INHERITOR_LIMIT = 6;
    private static final int PRODUCT_LIMIT = 4;
    private static final int COURSE_LIMIT = 4;
    private static final int ACTIVITY_LIMIT = 4;
    private static final int NEWS_LIMIT = 3;
    private static final int MAX_RECOMMENDED_LIMIT = 20;

    private final HomeMapper homeMapper;

    public HomeService(HomeMapper homeMapper) {
        this.homeMapper = homeMapper;
    }

    public HomeResponse getHome(String requestedCityCode) {
        CityDto city = resolveCity(requestedCityCode);
        String cityCode = city == null ? null : city.getCode();

        ZonedDateTime now = ZonedDateTime.now(HOME_ZONE);
        LocalDate weekStartDate = now.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStartDate.plusWeeks(1).atStartOfDay();

        HomeResponse response = new HomeResponse();
        response.setCity(city);
        response.setBanners(safeList(homeMapper.selectEnabledBanners(BANNER_LIMIT)));
        response.setCategories(getHeritageCategories(null));
        response.setHeritageProjects(safeList(
                homeMapper.selectRecommendedProjects(PROJECT_LIMIT, null)));
        response.setInheritors(safeList(
                homeMapper.selectRecommendedInheritors(INHERITOR_LIMIT)));
        response.setProducts(safeList(
                homeMapper.selectRecommendedProducts(PRODUCT_LIMIT)));
        response.setCourses(safeList(
                homeMapper.selectWeeklyCourses(cityCode, weekStart, weekEnd, COURSE_LIMIT)));
        response.setActivities(safeList(
                homeMapper.selectRecommendedActivities(
                        cityCode,
                        now.toLocalDateTime(),
                        ACTIVITY_LIMIT)));

        List<NewsSummaryDto> news = safeList(homeMapper.selectHomeNews(NEWS_LIMIT));
        news.forEach(item -> item.setSummary(toPlainText(item.getSummary(), 120)));
        response.setNews(news);
        return response;
    }

    public List<CityDto> getCities() {
        return safeList(homeMapper.selectActiveCities());
    }

    public List<CategoryDto> getHeritageCategories(Long parentId) {
        List<CategoryDto> categories = safeList(homeMapper.selectHeritageCategories());
        if (categories.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, List<CategoryDto>> childrenByParent = new HashMap<>();
        for (CategoryDto category : categories) {
            category.setChildren(new ArrayList<>());
            Long key = normalizeParentId(category.getParentId());
            childrenByParent.computeIfAbsent(key, ignored -> new ArrayList<>()).add(category);
        }

        if (parentId != null) {
            List<CategoryDto> children = childrenByParent.getOrDefault(parentId, Collections.emptyList());
            children.forEach(item -> populateChildren(item, childrenByParent, new HashSet<>()));
            return new ArrayList<>(children);
        }

        List<CategoryDto> roots = childrenByParent.getOrDefault(0L, Collections.emptyList());
        roots.forEach(item -> populateChildren(item, childrenByParent, new HashSet<>()));
        return new ArrayList<>(roots);
    }

    public List<HeritageLevelDto> getHeritageLevels() {
        return safeList(homeMapper.selectHeritageLevels());
    }

    public List<HeritageProjectSummaryDto> getRecommendedProjects(int limit, String regionCode) {
        if (limit < 1 || limit > MAX_RECOMMENDED_LIMIT) {
            throw new IllegalArgumentException("limit 必须在 1 到 20 之间");
        }
        return safeList(homeMapper.selectRecommendedProjects(limit, trim(regionCode)));
    }

    private CityDto resolveCity(String requestedCityCode) {
        String cityCode = trim(requestedCityCode);
        if (cityCode != null) {
            if (cityCode.length() > 32) {
                throw new IllegalArgumentException("cityCode 长度不能超过 32");
            }
            CityDto requested = homeMapper.selectCityByCode(cityCode);
            if (requested != null) {
                return requested;
            }
        }
        return homeMapper.selectDefaultCity();
    }

    private void populateChildren(CategoryDto category,
                                  Map<Long, List<CategoryDto>> childrenByParent,
                                  Set<Long> visited) {
        if (category.getId() == null || !visited.add(category.getId())) {
            category.setChildren(new ArrayList<>());
            return;
        }
        List<CategoryDto> children = new ArrayList<>(
                childrenByParent.getOrDefault(category.getId(), Collections.emptyList()));
        category.setChildren(children);
        children.forEach(child -> populateChildren(child, childrenByParent, new HashSet<>(visited)));
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private String toPlainText(String content, int maxLength) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String plainText = content
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
        if (plainText.length() <= maxLength) {
            return plainText;
        }
        return plainText.substring(0, maxLength) + "...";
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }
}
