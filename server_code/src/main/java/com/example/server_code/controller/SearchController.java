package com.example.server_code.controller;

import com.example.server_code.common.Result;
import com.example.server_code.dto.common.PageResult;
import com.example.server_code.dto.search.SearchItemDto;
import com.example.server_code.service.SearchService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@CrossOrigin
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public Result<PageResult<SearchItemDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String levelCode,
            @RequestParam(required = false) String regionCode,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            return Result.success(searchService.search(
                    keyword,
                    type,
                    categoryId,
                    levelCode,
                    regionCode,
                    page,
                    size));
        } catch (IllegalArgumentException exception) {
            return Result.error(400, exception.getMessage());
        }
    }
}
