package com.example.server_code.service;

import com.example.server_code.dto.common.PageResult;
import com.example.server_code.dto.search.SearchItemDto;
import com.example.server_code.mapper.SearchMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

@Service
public class SearchService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "all", "heritage_project", "inheritor", "product", "course");

    private final SearchMapper searchMapper;

    public SearchService(SearchMapper searchMapper) {
        this.searchMapper = searchMapper;
    }

    public PageResult<SearchItemDto> search(String keyword,
                                            String type,
                                            Long categoryId,
                                            String levelCode,
                                            String regionCode,
                                            int page,
                                            int size) {
        validatePage(page, size);
        if (categoryId != null && categoryId < 1) {
            throw new IllegalArgumentException("categoryId 必须为正整数");
        }

        String normalizedKeyword = trim(keyword);
        if (normalizedKeyword == null) {
            return PageResult.empty(page, size);
        }
        if (normalizedKeyword.length() > MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException("keyword 长度不能超过 100");
        }

        String normalizedType = normalizeType(type);
        String normalizedLevelCode = trim(levelCode);
        String normalizedRegionCode = trim(regionCode);
        long offset = (long) (page - 1) * size;

        long total = searchMapper.countSearchResults(
                normalizedKeyword,
                normalizedType,
                categoryId,
                normalizedLevelCode,
                normalizedRegionCode);
        if (total == 0) {
            return PageResult.empty(page, size);
        }

        var list = searchMapper.selectSearchResults(
                normalizedKeyword,
                normalizedType,
                categoryId,
                normalizedLevelCode,
                normalizedRegionCode,
                offset,
                size);
        if (list == null) {
            list = new ArrayList<>();
        }
        boolean hasNext = offset + list.size() < total;
        return new PageResult<>(list, total, page, size, hasNext);
    }

    private void validatePage(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("page 必须从 1 开始");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size 必须在 1 到 100 之间");
        }
    }

    private String normalizeType(String type) {
        String normalized = trim(type);
        if (normalized == null) {
            return "all";
        }
        normalized = normalized.toLowerCase(Locale.ROOT).replace('-', '_');
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new IllegalArgumentException(
                    "type 仅支持 all、heritage_project、inheritor、product、course");
        }
        return normalized;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
