package com.example.server_code.controller;

import com.example.server_code.common.Result;
import com.example.server_code.dto.home.HomeResponse;
import com.example.server_code.service.HomeService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@CrossOrigin
public class HomeController {
    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    public Result<HomeResponse> getHome(
            @RequestParam(required = false) String cityCode) {
        try {
            return Result.success(homeService.getHome(cityCode));
        } catch (IllegalArgumentException exception) {
            return Result.error(400, exception.getMessage());
        }
    }
}
