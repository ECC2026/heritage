package com.example.server_code.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.server_code.common.Result;
import com.example.server_code.entity.*;
import com.example.server_code.mapper.*;
import com.example.server_code.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class EntityController {
    
    @Autowired private UserMapper userMapper;
    @Autowired private NewsMapper newsMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private PerformanceMapper performanceMapper;
    @Autowired private ActivityMapper activityMapper;
    @Autowired private CategoryMapper categoryMapper;
    @Autowired private JwtUtil jwtUtil;
    
    // ==================== 用户管理 ====================
    
    /**
     * 分页查询所有用户。支持按用户名模糊搜索和手机号精确搜索，返回时自动去除password字段。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param username 用户名（模糊搜索）
     * @param phone 手机号（精确搜索）
     * @return { list, total, page, size }
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone) {
        
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        
        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username);
        }
        if (phone != null && !phone.isEmpty()) {
            wrapper.eq("phone", phone);
        }
        
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(item -> item.setPassword(null));
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }
    
    /**
     * 获取单个用户详情（不含password）。
     * @param id 用户ID
     * @return 用户对象
     */
    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 启用/禁用用户。
     * @param id 用户ID
     * @param params { status: 1-启用, 0-禁用 }
     * @return 成功无数据返回
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(params.get("status"));
        userMapper.updateById(user);
        return Result.success();
    }
    
    /**
     * 删除指定用户。
     * @param id 用户ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }
    
    // ==================== 资讯管理 ====================
    
    /**
     * 分页查询资讯列表。支持按标题模糊搜索和状态筛选，自动从HTML内容提取纯文本摘要。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param title 标题（模糊搜索）
     * @param status 状态筛选
     * @return { list, total, page, size }
     */
    @GetMapping("/news")
    public Result<Map<String, Object>> getNewsList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status) {
        
        Page<News> pageParam = new Page<>(page, size);
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        
        if (title != null && !title.isEmpty()) {
            wrapper.like("title", title);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        Page<News> result = newsMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::normalizeNews);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }
    
    /**
     * 获取单条资讯详情（含自动生成的纯文本摘要）。
     * @param id 资讯ID
     * @return 资讯对象
     */
    @GetMapping("/news/{id}")
    public Result<News> getNewsById(@PathVariable Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            return Result.error("资讯不存在");
        }
        normalizeNews(news);
        return Result.success(news);
    }
    
    /**
     * 新增一条资讯。
     * @param news 资讯JSON对象
     * @return 成功无数据返回
     */
    @PostMapping("/news")
    public Result<Void> addNews(@RequestBody News news) {
        newsMapper.insert(news);
        return Result.success();
    }
    
    /**
     * 修改指定资讯。
     * @param id 资讯ID
     * @param news 更新的资讯JSON对象
     * @return 成功无数据返回
     */
    @PutMapping("/news/{id}")
    public Result<Void> updateNews(@PathVariable Long id, @RequestBody News news) {
        news.setId(id);
        newsMapper.updateById(news);
        return Result.success();
    }
    
    /**
     * 删除指定资讯。
     * @param id 资讯ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/news/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        newsMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 上架/下架资讯。
     * @param id 资讯ID
     * @param params { status: 1-上架, 0-下架 }
     * @return 成功无数据返回
     */
    @PutMapping("/news/{id}/status")
    public Result<Void> updateNewsStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            return Result.error("资讯不存在");
        }
        news.setStatus(params.get("status"));
        newsMapper.updateById(news);
        return Result.success();
    }
    
    // ==================== 演出管理 ====================
    
    /**
     * 分页查询演出列表。支持按名称模糊搜索和状态筛选，按演出开始时间降序排列。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param name 演出名称（模糊搜索）
     * @param status 状态筛选
     * @return { list, total, page, size }
     */
    @GetMapping("/performances")
    public Result<Map<String, Object>> getPerformances(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        
        Page<Performance> pageParam = new Page<>(page, size);
        QueryWrapper<Performance> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("start_time");
        
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        Page<Performance> result = performanceMapper.selectPage(pageParam, wrapper);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }

    /**
     * 导出订单为CSV文件。含BOM头兼容Excel打开，列包含：ID、订单号、用户、收货人、电话、商品信息、状态、总价、地址、下单时间、支付时间、发货时间。
     * @param orderNo 订单号（模糊搜索，可选）
     * @param status 订单状态（可选）
     * @return CSV文件下载
     */
    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like("order_no", orderNo);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }

        List<Order> orders = orderMapper.selectList(wrapper);
        orders.forEach(this::normalizeOrder);

        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append("ID,订单号,用户,收货人,联系电话,商品信息,订单状态,总价,收货地址,下单时间,支付时间,发货时间\n");
        for (Order order : orders) {
            csv.append(order.getId()).append(",")
                    .append(escapeCsv(order.getOrderNo())).append(",")
                    .append(escapeCsv(order.getUserName())).append(",")
                    .append(escapeCsv(order.getReceiverName())).append(",")
                    .append(escapeCsv(order.getReceiverPhone())).append(",")
                    .append(escapeCsv(buildOrderItemsText(order))).append(",")
                    .append(escapeCsv(getOrderStatusText(order.getStatus()))).append(",")
                    .append(order.getTotalPrice() == null ? "" : order.getTotalPrice()).append(",")
                    .append(escapeCsv(order.getAddress())).append(",")
                    .append(escapeCsv(String.valueOf(order.getCreateTime()))).append(",")
                    .append(escapeCsv(String.valueOf(order.getPayTime()))).append(",")
                    .append(escapeCsv(String.valueOf(order.getShipTime())))
                    .append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取单个演出详情。
     * @param id 演出ID
     * @return 演出对象
     */
    @GetMapping("/performances/{id}")
    public Result<Performance> getPerformanceById(@PathVariable Long id) {
        Performance performance = performanceMapper.selectById(id);
        if (performance == null) {
            return Result.error("演出不存在");
        }
        return Result.success(performance);
    }

    /**
     * 新增一场演出。
     * @param performance 演出JSON对象
     * @return 成功无数据返回
     */
    @PostMapping("/performances")
    public Result<Void> addPerformance(@RequestBody Performance performance) {
        performanceMapper.insert(performance);
        return Result.success();
    }

    /**
     * 修改指定演出。
     * @param id 演出ID
     * @param performance 更新的演出JSON对象
     * @return 成功无数据返回
     */
    @PutMapping("/performances/{id}")
    public Result<Void> updatePerformance(@PathVariable Long id, @RequestBody Performance performance) {
        performance.setId(id);
        performanceMapper.updateById(performance);
        return Result.success();
    }

    /**
     * 删除指定演出。
     * @param id 演出ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/performances/{id}")
    public Result<Void> deletePerformance(@PathVariable Long id) {
        performanceMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 上架/下架演出。
     * @param id 演出ID
     * @param params { status: 1-上架, 0-下架 }
     * @return 成功无数据返回
     */
    @PutMapping("/performances/{id}/status")
    public Result<Void> updatePerformanceStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Performance performance = performanceMapper.selectById(id);
        if (performance == null) {
            return Result.error("演出不存在");
        }
        performance.setStatus(params.get("status"));
        performanceMapper.updateById(performance);
        return Result.success();
    }
    
    // ==================== 商品管理 ====================
    
    /**
     * 分页查询商品列表。支持按名称模糊搜索和状态筛选，自动关联查询分类名称。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param name 商品名称（模糊搜索）
     * @param status 状态筛选
     * @return { list, total, page, size }
     */
    @GetMapping("/products")
    public Result<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        
        Page<Product> pageParam = new Page<>(page, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        Page<Product> result = productMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::normalizeProduct);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }
    
    /**
     * 获取单个商品详情（含分类名称）。
     * @param id 商品ID
     * @return 商品对象
     */
    @GetMapping("/products/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        normalizeProduct(product);
        return Result.success(product);
    }
    
    /**
     * 新增商品。若传入的分类名称在数据库中不存在则自动创建该分类。
     * @param product 商品JSON对象
     * @return 成功无数据返回
     */
    @PostMapping("/products")
    public Result<Void> addProduct(@RequestBody Product product) {
        fillProductCategory(product);
        productMapper.insert(product);
        return Result.success();
    }
    
    /**
     * 修改指定商品。
     * @param id 商品ID
     * @param product 更新的商品JSON对象
     * @return 成功无数据返回
     */
    @PutMapping("/products/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        fillProductCategory(product);
        productMapper.updateById(product);
        return Result.success();
    }
    
    /**
     * 删除指定商品。
     * @param id 商品ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.success();
    }
    
    /**
     * 上架/下架商品。
     * @param id 商品ID
     * @param params { status: 1-上架, 0-下架 }
     * @return 成功无数据返回
     */
    @PutMapping("/products/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        product.setStatus(params.get("status"));
        productMapper.updateById(product);
        return Result.success();
    }
    
    /**
     * 修改商品库存数量。
     * @param id 商品ID
     * @param params { stock: number }
     * @return 成功无数据返回
     */
    @PutMapping("/products/{id}/stock")
    public Result<Void> updateProductStock(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        product.setStock(params.get("stock"));
        productMapper.updateById(product);
        return Result.success();
    }
    
    // ==================== 订单管理 ====================
    
    /**
     * 分页查询所有订单（管理端）。支持按订单号模糊搜索和状态筛选，自动关联用户昵称和订单商品明细。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @param orderNo 订单号（模糊搜索）
     * @param status 订单状态筛选：0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消
     * @return { list, total, page, size }
     */
    @GetMapping("/orders")
    public Result<Map<String, Object>> getOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {
        
        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like("order_no", orderNo);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::normalizeOrder);
        
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        
        return Result.success(data);
    }
    
    /**
     * 获取单个订单详情（含关联的用户信息和商品明细）。
     * @param id 订单ID
     * @return 订单对象
     */
    @GetMapping("/orders/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        normalizeOrder(order);
        return Result.success(order);
    }

    /**
     * 用户下单（需登录）。校验库存、计算总价、自动生成订单号（ORD+时间戳+UUID），扣减库存并增加销量。
     * @param params { items: [{ productId, quantity }], address?, receiverName?, receiverPhone?, remark? }
     * @return 成功无数据返回
     */
    @PostMapping("/orders")
    public Result<Void> createOrder(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Object itemsObj = params.get("items");
        if (!(itemsObj instanceof List<?> items) || items.isEmpty()) {
            return Result.error("订单商品不能为空");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (Object itemObj : items) {
            if (!(itemObj instanceof Map<?, ?> itemMap)) {
                continue;
            }
            Long productId = toLong(itemMap.get("productId"));
            Integer quantity = toInt(itemMap.get("quantity"), 1);
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return Result.error("存在无效商品");
            }
            if (product.getStock() != null && product.getStock() < quantity) {
                return Result.error(product.getName() + " 库存不足");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(productId);
            orderItem.setProductName(product.getName());
            orderItem.setCover(product.getCover());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(quantity);
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            orderItems.add(orderItem);
            totalPrice = totalPrice.add(orderItem.getSubtotal());
        }

        Order order = new Order();
        order.setOrderNo("ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4));
        order.setUserId(user.getId());
        order.setTotalPrice(totalPrice);
        order.setAddress(stringValue(params.get("address"), "待补充收货地址"));
        order.setReceiverName(stringValue(params.get("receiverName"), displayName(user)));
        order.setReceiverPhone(stringValue(params.get("receiverPhone"), user.getPhone()));
        order.setRemark(stringValue(params.get("remark"), null));
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.insert(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItemMapper.insert(orderItem);

            Product product = productMapper.selectById(orderItem.getProductId());
            int stock = product.getStock() == null ? 0 : product.getStock();
            int sales = product.getSales() == null ? 0 : product.getSales();
            product.setStock(Math.max(0, stock - orderItem.getQuantity()));
            product.setSales(sales + orderItem.getQuantity());
            productMapper.updateById(product);
        }

        return Result.success();
    }

    /**
     * 当前登录用户查看自己的订单列表。
     * @param page 页码，默认1
     * @param size 每页条数，默认10
     * @return { list, total, page, size }
     */
    @GetMapping("/orders/my")
    public Result<Map<String, Object>> getMyOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        User user = getCurrentUser(request);
        if (user == null) {
            return Result.error(401, "请先登录");
        }

        Page<Order> pageParam = new Page<>(page, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId()).orderByDesc("create_time");
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(this::normalizeOrder);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", result.getCurrent());
        data.put("size", result.getSize());
        return Result.success(data);
    }
    
    /**
     * 修改订单状态。设为已发货(status=2)时自动记录发货时间。
     * @param id 订单ID
     * @param params { status: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消 }
     * @return 成功无数据返回
     */
    @PutMapping("/orders/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setStatus(params.get("status"));
        if (params.get("status") != null && params.get("status") == 2) {
            order.setShipTime(LocalDateTime.now());
        }
        orderMapper.updateById(order);
        return Result.success();
    }
    
    /**
     * 删除订单，同时删除关联的订单明细。
     * @param id 订单ID
     * @return 成功无数据返回
     */
    @DeleteMapping("/orders/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        orderMapper.deleteById(id);
        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", id);
        orderItemMapper.delete(wrapper);
        return Result.success();
    }
    
    // ==================== 统计接口 ====================
    
    /**
     * 获取运营统计数据：用户总数、资讯总数、商品总数、订单总数、活动总数，用于管理后台仪表盘。
     * @return { userCount, newsCount, productCount, orderCount, activityCount }
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> data = new HashMap<>();
        
        // 统计用户数量
        Long userCount = userMapper.selectCount(null);
        data.put("userCount", userCount);
        
        // 统计资讯数量
        Long newsCount = newsMapper.selectCount(null);
        data.put("newsCount", newsCount);
        
        // 统计商品数量
        Long productCount = productMapper.selectCount(null);
        data.put("productCount", productCount);
        
        // 统计订单数量
        Long orderCount = orderMapper.selectCount(null);
        data.put("orderCount", orderCount);

        Long activityCount = activityMapper.selectCount(null);
        data.put("activityCount", activityCount);
        
        return Result.success(data);
    }

    private void normalizeNews(News news) {
        if (news.getContent() == null) {
            news.setSummary("");
            return;
        }
        String plainText = news.getContent().replaceAll("<[^>]+>", "").replace("&nbsp;", " ").trim();
        news.setSummary(plainText.length() > 60 ? plainText.substring(0, 60) + "..." : plainText);
    }

    private void fillProductCategory(Product product) {
        if (product.getCategory() == null || product.getCategory().isBlank()) {
            return;
        }
        String categoryName = product.getCategory().trim();
        if (categoryName.isEmpty()) {
            return;
        }
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.eq("name", categoryName).orderByAsc("id").last("LIMIT 1");
        List<Category> categories = categoryMapper.selectList(wrapper);
        Category category = categories.isEmpty() ? null : categories.get(0);
        if (category == null) {
            category = new Category();
            category.setName(categoryName);
            category.setParentId(0L);
            category.setSort(0);
            category.setStatus(1);
            categoryMapper.insert(category);
        }
        product.setCategory(categoryName);
        product.setCategoryId(category.getId());
    }

    private void normalizeProduct(Product product) {
        if (product.getCategoryId() == null) {
            return;
        }
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            product.setCategory(category.getName());
        }
    }

    private void normalizeOrder(Order order) {
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            order.setUserName(displayName(user));
        }
        order.setPhone(order.getReceiverPhone());

        QueryWrapper<OrderItem> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", order.getId()).orderByAsc("id");
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        order.setItems(items);
        if (!items.isEmpty()) {
            OrderItem firstItem = items.get(0);
            order.setProductName(firstItem.getProductName());
            order.setQuantity(firstItem.getQuantity());
        }
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

    private String displayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return "用户" + user.getId();
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

    private String stringValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String result = String.valueOf(value).trim();
        return result.isEmpty() ? defaultValue : result;
    }

    private String buildOrderItemsText(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return order.getProductName();
        }
        List<String> itemTexts = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            itemTexts.add(item.getProductName() + " x " + item.getQuantity());
        }
        return String.join(" ; ", itemTexts);
    }

    private String getOrderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已发货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            default -> "未知";
        };
    }

    private String escapeCsv(String value) {
        if (value == null || "null".equalsIgnoreCase(value)) {
            return "\"\"";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
