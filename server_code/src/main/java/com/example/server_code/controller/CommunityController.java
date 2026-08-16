package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.Comment;
import com.example.server_code.entity.Post;
import com.example.server_code.entity.PostLike;
import com.example.server_code.entity.User;
import com.example.server_code.mapper.CommentMapper;
import com.example.server_code.mapper.PostMapper;
import com.example.server_code.mapper.PostLikeMapper;
import com.example.server_code.mapper.UserMapper;
import com.example.server_code.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class CommunityController {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private PostLikeMapper postLikeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 分页查询帖子列表（仅status=1的公开帖）。置顶帖优先，然后按创建时间降序。支持按分类筛选。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param category 分类筛选（可选）
     * @return { list, total, page, size }
     */
    @GetMapping("/posts")
    public Result<Map<String, Object>> getPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category) {

        Page<Post> pageParam = new Page<>(page, size);
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("is_top").orderByDesc("create_time");
        if (category != null && !category.isBlank()) {
            wrapper.eq("category", category);
        }

        Page<Post> result = postMapper.selectPage(pageParam, wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    /**
     * 当前登录用户查看自己发布的所有帖子。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return { list, total, page, size }
     */
    @GetMapping("/posts/my")
    public Result<Map<String, Object>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Page<Post> pageParam = new Page<>(page, size);
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId()).orderByDesc("create_time");
        Page<Post> result = postMapper.selectPage(pageParam, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    /**
     * 获取帖子详情。每次访问自动增加1次浏览量。
     * @param id 帖子ID
     * @return 帖子对象
     */
    @GetMapping("/posts/{id}")
    public Result<Post> getPostDetail(@PathVariable Long id) {
        Post post = postMapper.selectById(id);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            return Result.error("帖子不存在");
        }
        post.setViews((post.getViews() == null ? 0 : post.getViews()) + 1);
        postMapper.updateById(post);
        return Result.success(post);
    }

    /**
     * 发布帖子（需登录）。自动填充用户信息（昵称、头像），初始化浏览量/点赞数/评论数为0。
     * @param post { title?, content, images?, category? } content为必填
     * @return 成功无数据返回
     */
    @PostMapping("/posts")
    public Result<Void> publishPost(@RequestBody Post post, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        if (post.getContent() == null || post.getContent().isBlank()) {
            return Result.error("帖子内容不能为空");
        }

        post.setUserId(user.getId());
        post.setUserName(displayName(user));
        post.setUserAvatar(user.getAvatar());
        post.setViews(0);
        post.setLikes(0);
        post.setComments(0);
        post.setStatus(1);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        postMapper.insert(post);
        return Result.success();
    }

    /**
     * 点赞/取消点赞帖子（需登录）。同一用户重复请求会切换点赞状态。
     * @param id 帖子ID
     * @return { liked: boolean, likes: number }
     */
    @PostMapping("/posts/{id}/like")
    public Result<Map<String, Object>> togglePostLike(@PathVariable Long id, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        QueryWrapper<PostLike> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", id).eq("user_id", user.getId());
        PostLike exists = postLikeMapper.selectOne(wrapper);

        boolean liked;
        int likes = post.getLikes() == null ? 0 : post.getLikes();
        if (exists == null) {
            PostLike postLike = new PostLike();
            postLike.setPostId(id);
            postLike.setUserId(user.getId());
            postLikeMapper.insert(postLike);
            post.setLikes(likes + 1);
            liked = true;
        } else {
            postLikeMapper.deleteById(exists.getId());
            post.setLikes(Math.max(0, likes - 1));
            liked = false;
        }
        postMapper.updateById(post);

        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        data.put("likes", post.getLikes());
        return Result.success(data);
    }

    /**
     * 删除帖子（需登录，仅作者本人可删除）。同时删除该帖子的所有关联评论。
     * @param id 帖子ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Post post = postMapper.selectById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        if (!user.getId().equals(post.getUserId())) {
            return Result.error(403, "无权删除该帖子");
        }

        postMapper.deleteById(id);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", id);
        commentMapper.delete(wrapper);
        return Result.success();
    }

    /**
     * 分页查询指定帖子的评论列表（仅status=1的公开评论），按创建时间升序排列。
     * @param postId 帖子ID（必填）
     * @param page 页码，默认1
     * @param size 每页条数，默认20
     * @return { list, total, page, size }
     */
    @GetMapping("/comments")
    public Result<Map<String, Object>> getComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<Comment> pageParam = new Page<>(page, size);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", postId).eq("status", 1).orderByAsc("create_time");
        Page<Comment> result = commentMapper.selectPage(pageParam, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    /**
     * 发表评论（需登录）。自动关联用户信息（昵称、头像），同时更新帖子的评论计数+1。
     * @param comment { postId, content, parentId? } content为必填
     * @return 成功无数据返回
     */
    @PostMapping("/comments")
    public Result<Void> publishComment(@RequestBody Comment comment, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }
        if (comment.getPostId() == null || comment.getContent() == null || comment.getContent().isBlank()) {
            return Result.error("评论内容不能为空");
        }

        Post post = postMapper.selectById(comment.getPostId());
        if (post == null) {
            return Result.error("帖子不存在");
        }

        comment.setUserId(user.getId());
        comment.setUserName(displayName(user));
        comment.setUserAvatar(user.getAvatar());
        comment.setLikes(0);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        post.setComments((post.getComments() == null ? 0 : post.getComments()) + 1);
        postMapper.updateById(post);
        return Result.success();
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
        Long userId = jwtUtil.getUserIdFromToken(token);
        return userMapper.selectById(userId);
    }

    private String displayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return "用户" + user.getId();
    }
}
