package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.server_code.common.Result;
import com.example.server_code.entity.Cart;
import com.example.server_code.entity.Product;
import com.example.server_code.mapper.CartMapper;
import com.example.server_code.mapper.ProductMapper;
import com.example.server_code.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 添加商品到购物车（需登录）。若商品已在购物车中则累加数量，否则新增记录。会校验商品是否存在及是否下架。
     * @param params { productId, quantity? } 默认数量1
     * @return 成功无数据返回
     */
    @PostMapping
    public Result<Void> addCart(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        Long productId = toLong(params.get("productId"));
        Integer quantity = toInt(params.get("quantity"), 1);
        if (productId == null) {
            return Result.error("商品不存在");
        }

        Product product = productMapper.selectById(productId);
        if (product == null || (product.getStatus() != null && product.getStatus() == 0)) {
            return Result.error("商品不存在或已下架");
        }

        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("product_id", productId);
        Cart cart = cartMapper.selectOne(wrapper);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(Math.max(quantity, 1));
            cartMapper.insert(cart);
        } else {
            cart.setQuantity(cart.getQuantity() + Math.max(quantity, 1));
            cartMapper.updateById(cart);
        }
        return Result.success();
    }

    /**
     * 获取当前登录用户的购物车列表，自动关联查询商品的详细信息（名称、封面、价格、库存）。
     * @return 购物车列表（每个条目含关联的Product对象）
     */
    @GetMapping
    public Result<List<Cart>> getCartList(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        QueryWrapper<Cart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        List<Cart> list = cartMapper.selectList(wrapper);
        for (Cart cart : list) {
            cart.setProduct(productMapper.selectById(cart.getProductId()));
        }
        return Result.success(list);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
