package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.*;
import com.example.server_code.mapper.*;
import com.example.server_code.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin
public class FavoriteController {

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private NewsMapper newsMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 查询当前用户对某个目标（资讯/商品/活动/帖子）是否已收藏。未登录时返回favorited=false。
     * @param type 目标类型：news|product|activity|post
     * @param targetId 目标ID
     * @return { favorited: boolean }
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus(
            @RequestParam String type,
            @RequestParam Long targetId,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        Map<String, Object> data = new HashMap<>();
        if (user == null) {
            data.put("favorited", false);
            return Result.success(data);
        }

        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId()).eq("type", type).eq("target_id", targetId);
        data.put("favorited", favoriteMapper.selectCount(wrapper) > 0);
        return Result.success(data);
    }

    /**
     * 获取当前用户的收藏统计：总数和各类型的收藏数量。
     * @return { total, news, product, activity, post }
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        List<Favorite> favorites = favoriteMapper.selectList(new QueryWrapper<Favorite>().eq("user_id", user.getId()));
        Map<String, Object> data = new HashMap<>();
        data.put("total", favorites.size());
        data.put("news", countByType(favorites, "news"));
        data.put("product", countByType(favorites, "product"));
        data.put("activity", countByType(favorites, "activity"));
        data.put("post", countByType(favorites, "post"));
        return Result.success(data);
    }

    /**
     * 分页查询我的收藏列表。支持按类型筛选，自动关联查询收藏目标的标题、封面等详细信息。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param type 类型筛选：news|product|activity|post（可选）
     * @return { list: [{ id, type, targetId, title, cover, subTitle, summary, ... }], total, page, size }
     */
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Page<Favorite> pageParam = new Page<>(page, size);
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId()).orderByDesc("create_time");
        if (type != null && !type.isBlank()) {
            wrapper.eq("type", type);
        }
        Page<Favorite> result = favoriteMapper.selectPage(pageParam, wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Favorite favorite : result.getRecords()) {
            Map<String, Object> item = buildFavoriteItem(favorite);
            if (!item.isEmpty()) {
                list.add(item);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    /**
     * 切换收藏状态（需登录）。未收藏则添加收藏，已收藏则取消收藏。会校验收藏目标是否存在。
     * @param params { type: "news"|"product"|"activity"|"post", targetId: number }
     * @return { favorited: boolean }
     */
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleFavorite(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        String type = params.get("type") == null ? null : String.valueOf(params.get("type")).trim();
        Long targetId = params.get("targetId") == null ? null : Long.parseLong(String.valueOf(params.get("targetId")));
        if (type == null || targetId == null) {
            return Result.error("收藏参数不完整");
        }
        if (!existsTarget(type, targetId)) {
            return Result.error("收藏目标不存在");
        }

        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId()).eq("type", type).eq("target_id", targetId);
        Favorite exists = favoriteMapper.selectOne(wrapper);

        boolean favorited;
        if (exists == null) {
            Favorite favorite = new Favorite();
            favorite.setUserId(user.getId());
            favorite.setType(type);
            favorite.setTargetId(targetId);
            favoriteMapper.insert(favorite);
            favorited = true;
        } else {
            favoriteMapper.deleteById(exists.getId());
            favorited = false;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("favorited", favorited);
        return Result.success(data);
    }

    private int countByType(List<Favorite> favorites, String type) {
        return (int) favorites.stream().filter(item -> type.equals(item.getType())).count();
    }

    private boolean existsTarget(String type, Long targetId) {
        return switch (type) {
            case "news" -> newsMapper.selectById(targetId) != null;
            case "product" -> productMapper.selectById(targetId) != null;
            case "activity" -> activityMapper.selectById(targetId) != null;
            case "post" -> postMapper.selectById(targetId) != null;
            default -> false;
        };
    }

    private Map<String, Object> buildFavoriteItem(Favorite favorite) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", favorite.getId());
        item.put("type", favorite.getType());
        item.put("targetId", favorite.getTargetId());
        item.put("createTime", favorite.getCreateTime());

        switch (favorite.getType()) {
            case "news" -> {
                News news = newsMapper.selectById(favorite.getTargetId());
                if (news == null) return Map.of();
                item.put("title", news.getTitle());
                item.put("cover", news.getCover());
                item.put("subTitle", news.getCategory());
                item.put("summary", news.getContent() == null ? "" : news.getContent().replaceAll("<[^>]+>", "").trim());
            }
            case "product" -> {
                Product product = productMapper.selectById(favorite.getTargetId());
                if (product == null) return Map.of();
                item.put("title", product.getName());
                item.put("cover", product.getCover());
                item.put("subTitle", "¥" + product.getPrice());
                item.put("summary", product.getDescription());
            }
            case "activity" -> {
                Activity activity = activityMapper.selectById(favorite.getTargetId());
                if (activity == null) return Map.of();
                item.put("title", activity.getName());
                item.put("cover", activity.getCover());
                item.put("subTitle", activity.getLocation());
                item.put("summary", activity.getDescription());
                item.put("startTime", activity.getStartTime());
            }
            case "post" -> {
                Post post = postMapper.selectById(favorite.getTargetId());
                if (post == null) return Map.of();
                item.put("title", post.getTitle() == null || post.getTitle().isBlank() ? "社区动态" : post.getTitle());
                item.put("cover", firstImage(post.getImages(), post.getUserAvatar()));
                item.put("subTitle", post.getCategory());
                item.put("summary", post.getContent());
            }
            default -> {
                return Map.of();
            }
        }
        return item;
    }

    private String firstImage(String images, String fallback) {
        if (images == null || images.isBlank()) {
            return fallback;
        }
        return images.split(",")[0];
    }

    private User getCurrentUser(HttpServletRequest request) {
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
        return userMapper.selectById(jwtUtil.getUserIdFromToken(token));
    }
}
