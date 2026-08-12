package com.example.server_code.controller;

import com.example.server_code.common.Result;
import com.example.server_code.entity.ProductSystem;
import com.example.server_code.service.ProductSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 产品体系（公开端）：仅返回启用体系，供筛选与展示。
 */
@RestController
@RequestMapping("/api/product-systems")
@CrossOrigin
public class ProductSystemController {

    @Autowired
    private ProductSystemService productSystemService;

    @GetMapping
    public Result<List<ProductSystem>> listEnabled() {
        return Result.success(productSystemService.listEnabled());
    }
}
