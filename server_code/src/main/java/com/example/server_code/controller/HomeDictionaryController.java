package com.example.server_code.controller;

import com.example.server_code.common.Result;
import com.example.server_code.dto.home.CategoryDto;
import com.example.server_code.dto.home.CityDto;
import com.example.server_code.dto.home.HeritageLevelDto;
import com.example.server_code.service.HomeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class HomeDictionaryController {
    private final HomeService homeService;

    public HomeDictionaryController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/cities")
    public Result<List<CityDto>> getCities() {
        return Result.success(homeService.getCities());
    }

    @GetMapping("/heritage-categories")
    public Result<List<CategoryDto>> getHeritageCategories(
            @RequestParam(required = false) Long parentId) {
        return Result.success(homeService.getHeritageCategories(parentId));
    }

    @GetMapping("/heritage-levels")
    public Result<List<HeritageLevelDto>> getHeritageLevels() {
        return Result.success(homeService.getHeritageLevels());
    }
}
