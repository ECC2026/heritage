

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity
-- ----------------------------
DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动名称',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '活动描述',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `organizer_id` bigint NULL DEFAULT NULL COMMENT '组织者ID（传承人）',
  `organizer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组织者名称',
  `limit_count` int NULL DEFAULT 0 COMMENT '限制人数',
  `signup_count` int NULL DEFAULT 0 COMMENT '已报名人数',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审核，1-进行中，2-已结束',
  `type` tinyint NULL DEFAULT 0 COMMENT '类型：0-平台官方，1-传承人发起',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_organizer_id`(`organizer_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity
-- ----------------------------
INSERT INTO `activity` VALUES (1, '蜀绣体验活动', '/uploads/activity_001.jpg', '近距离体验蜀绣技艺，感受传统文化的魅力', '成都非遗体验中心', '2026-05-10 09:00:00', NULL, 1, '张大师', 30, 18, 1, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `activity` VALUES (2, '川剧变脸表演', '/uploads/activity_002.jpg', '观看经典川剧变脸表演，参与互动游戏', '成都锦江剧场', '2026-05-20 19:00:00', NULL, NULL, '成都市川剧院', 200, 156, 1, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `activity` VALUES (3, '竹编技艺培训', '/uploads/activity_003.jpg', '竹编技艺基础培训，适合初学者', '崇州竹艺村', '2026-06-05 10:00:00', NULL, NULL, '崇州文旅局', 20, 8, 1, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `activity` VALUES (4, '蜀绣体验活动', '/uploads/activity_001.jpg', '近距离体验蜀绣技艺，感受传统文化的魅力', '成都非遗体验中心', '2026-05-10 09:00:00', NULL, 1, '张大师', 30, 18, 1, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `activity` VALUES (5, '川剧变脸表演', '/uploads/activity_002.jpg', '观看经典川剧变脸表演，参与互动游戏', '成都锦江剧场', '2026-05-20 19:00:00', NULL, NULL, '成都市川剧院', 200, 156, 1, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `activity` VALUES (6, '竹编技艺培训', '/uploads/activity_003.jpg', '竹编技艺基础培训，适合初学者', '崇州竹艺村', '2026-06-05 10:00:00', NULL, NULL, '崇州文旅局', 20, 8, 1, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (3, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800138000', 'admin@example.com', NULL, 1, '2026-04-08 22:14:04', '0:0:0:0:0:0:0:1', '2026-04-08 22:08:12', '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for banner
-- ----------------------------
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
  `link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `link_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链接类型：news、activity、product等',
  `sort` int NULL DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banner
-- ----------------------------
INSERT INTO `banner` VALUES (1, '首页大图1', '/uploads/banner_001.jpg', '/pages/index/index', 'page', 1, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `banner` VALUES (2, '非遗传承', '/uploads/banner_002.jpg', '/pages/heritage/list', 'page', 2, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `banner` VALUES (3, '文创商城', '/uploads/banner_003.jpg', '/pages/shop/list', 'page', 3, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `banner` VALUES (4, '首页大图1', '/uploads/banner_001.jpg', '/pages/index/index', 'page', 1, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `banner` VALUES (5, '非遗传承', '/uploads/banner_002.jpg', '/pages/heritage/list', 'page', 2, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `banner` VALUES (6, '文创商城', '/uploads/banner_003.jpg', '/pages/shop/list', 'page', 3, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_product`(`user_id` ASC, `product_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart
-- ----------------------------

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父级ID',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '非遗手工艺品', 0, 1, 1, '2026-04-07 02:24:15');
INSERT INTO `category` VALUES (2, '非遗文创', 0, 2, 1, '2026-04-07 02:24:15');
INSERT INTO `category` VALUES (3, '非遗服饰', 0, 3, 1, '2026-04-07 02:24:15');
INSERT INTO `category` VALUES (4, '非遗食品', 0, 4, 1, '2026-04-07 02:24:15');
INSERT INTO `category` VALUES (5, '非遗手工艺品', 0, 1, 1, '2026-04-08 22:08:12');
INSERT INTO `category` VALUES (6, '非遗文创', 0, 2, 1, '2026-04-08 22:08:12');
INSERT INTO `category` VALUES (7, '非遗服饰', 0, 3, 1, '2026-04-08 22:08:12');
INSERT INTO `category` VALUES (8, '非遗食品', 0, 4, 1, '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名（冗余）',
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像（冗余）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论ID（回复）',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '回复目标用户ID',
  `reply_to_user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '回复目标用户名',
  `likes` int NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-待审核，1-已发布，2-已删除',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 1, 2, '文化守护者', NULL, '加油！我也是从零开始的，多练练就好了', NULL, NULL, NULL, 0, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `comment` VALUES (2, 1, 3, '手艺传承人', NULL, '建议先从简单的针法开始练习', NULL, NULL, NULL, 0, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `comment` VALUES (3, 2, 1, '非遗爱好者', NULL, '收藏了，周末就去', NULL, NULL, NULL, 0, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `comment` VALUES (4, 1, 2, '文化守护者', NULL, '加油！我也是从零开始的，多练练就好了', NULL, NULL, NULL, 0, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `comment` VALUES (5, 1, 3, '手艺传承人', NULL, '建议先从简单的针法开始练习', NULL, NULL, NULL, 0, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `comment` VALUES (6, 2, 1, '非遗爱好者', NULL, '收藏了，周末就去', NULL, NULL, NULL, 0, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `comment_id` bigint NOT NULL COMMENT '评论ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_comment_id`(`comment_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_like
-- ----------------------------

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收藏类型：news、activity、product、post',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_target`(`user_id` ASC, `type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (1, 1, 'news', 4, '2026-04-09 19:36:29');

-- ----------------------------
-- Table structure for heritage_project
-- ----------------------------
DROP TABLE IF EXISTS `heritage_project`;
CREATE TABLE `heritage_project`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '项目名称',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '项目描述',
  `region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属地区',
  `level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '非遗级别：国家级、省级、市级、县级',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort`(`sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '热门非遗项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of heritage_project
-- ----------------------------
INSERT INTO `heritage_project` VALUES (1, '蜀绣', '/uploads/heritage_001.jpg', '传统美术', '蜀绣是中国刺绣传承时间最长的绣种之一，与苏绣、湘绣、粤绣齐名。', '四川省成都市', '国家级', 1, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `heritage_project` VALUES (2, '川剧变脸', '/uploads/heritage_002.jpg', '传统戏剧', '川剧变脸是川剧表演的精髓之一，通过快速变换脸谱来表现人物情绪。', '四川省', '国家级', 2, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `heritage_project` VALUES (3, '竹编技艺', '/uploads/heritage_003.jpg', '传统技艺', '竹编是用竹条篾片编成的生活和生产用品的手工艺。', '四川省崇州市', '省级', 3, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `heritage_project` VALUES (4, '银花丝', '/uploads/heritage_004.jpg', '传统美术', '成都银花丝是成都代表性的金属工艺珍品。', '四川省成都市', '国家级', 4, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `heritage_project` VALUES (5, '藏族编织', '/uploads/heritage_005.jpg', '传统技艺', '藏族编织是藏族人民传统的手工艺，具有浓郁的民族特色。', '四川省甘孜州', '省级', 5, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `heritage_project` VALUES (6, '蜀绣', '/uploads/heritage_001.jpg', '传统美术', '蜀绣是中国刺绣传承时间最长的绣种之一，与苏绣、湘绣、粤绣齐名。', '四川省成都市', '国家级', 1, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `heritage_project` VALUES (7, '川剧变脸', '/uploads/heritage_002.jpg', '传统戏剧', '川剧变脸是川剧表演的精髓之一，通过快速变换脸谱来表现人物情绪。', '四川省', '国家级', 2, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `heritage_project` VALUES (8, '竹编技艺', '/uploads/heritage_003.jpg', '传统技艺', '竹编是用竹条篾片编成的生活和生产用品的手工艺。', '四川省崇州市', '省级', 3, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `heritage_project` VALUES (9, '银花丝', '/uploads/heritage_004.jpg', '传统美术', '成都银花丝是成都代表性的金属工艺珍品。', '四川省成都市', '国家级', 4, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `heritage_project` VALUES (10, '藏族编织', '/uploads/heritage_005.jpg', '传统技艺', '藏族编织是藏族人民传统的手工艺，具有浓郁的民族特色。', '四川省甘孜州', '省级', 5, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for inheritor
-- ----------------------------
DROP TABLE IF EXISTS `inheritor`;
CREATE TABLE `inheritor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '传承人ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `skill_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '技艺类型',
  `skill_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '技艺简介',
  `experience` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '从业经历',
  `certificate` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '资质证书图片',
  `作品展示` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作品展示图片',
  `audit_status` tinyint NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-已通过，2-未通过',
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核备注',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '非遗传承人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inheritor
-- ----------------------------
INSERT INTO `inheritor` VALUES (1, 3, '张大师', '13800001003', NULL, '蜀绣', '从事蜀绣技艺40年，国家级非遗传承人', NULL, '/uploads/certificate_001.jpg', NULL, 1, NULL, NULL, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `inheritor` VALUES (2, 3, '张大师', '13800001003', NULL, '蜀绣', '从事蜀绣技艺40年，国家级非遗传承人', NULL, '/uploads/certificate_001.jpg', NULL, 1, NULL, NULL, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for news
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资讯ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类：非遗动态、传承人风采、活动回顾、政策解读',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作者',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来源',
  `views` int NULL DEFAULT 0 COMMENT '浏览量',
  `likes` int NULL DEFAULT 0 COMMENT '点赞数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已下架',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '非遗资讯表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of news
-- ----------------------------
INSERT INTO `news` VALUES (1, '蜀绣成功入选国家级非遗名录', '/uploads/news_cover_001.jpg', '<p>经过层层评审，蜀绣近日成功入选国家级非物质文化遗产名录，标志着这一传统技艺得到了国家层面的认可和保护。</p><p>蜀绣历史悠久，起源于川西地区，以其精湛的针法、绚丽的色彩而闻名于世。</p>', '非遗动态', '文化部', NULL, 1256, 328, 1, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `news` VALUES (2, '著名传承人张大师荣获终身成就奖', '/uploads/news_cover_002.jpg', '<p>在刚刚结束的全国非遗传承人表彰大会上，著名蜀绣传承人张大师荣获\"非遗传承终身成就奖\"。</p><p>张大师从事蜀绣技艺40余年，培养了大批年轻传承人。</p>', '传承人风采', '记者小李', NULL, 892, 215, 1, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `news` VALUES (3, '川剧变脸进校园活动圆满成功', '/uploads/news_cover_003.jpg', '<p>为期一周的\"川剧变脸进校园\"活动在成都市各大中小学圆满结束，近万名学生近距离感受了川剧的独特魅力。</p>', '活动回顾', '教育局', NULL, 654, 189, 1, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `news` VALUES (4, '蜀绣成功入选国家级非遗名录', '/uploads/news_cover_001.jpg', '<p>经过层层评审，蜀绣近日成功入选国家级非物质文化遗产名录，标志着这一传统技艺得到了国家层面的认可和保护。</p><p>蜀绣历史悠久，起源于川西地区，以其精湛的针法、绚丽的色彩而闻名于世。</p>', '非遗动态', '文化部', NULL, 1256, 328, 1, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `news` VALUES (5, '著名传承人张大师荣获终身成就奖', '/uploads/news_cover_002.jpg', '<p>在刚刚结束的全国非遗传承人表彰大会上，著名蜀绣传承人张大师荣获\"非遗传承终身成就奖\"。</p><p>张大师从事蜀绣技艺40余年，培养了大批年轻传承人。</p>', '传承人风采', '记者小李', NULL, 892, 215, 1, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `news` VALUES (6, '川剧变脸进校园活动圆满成功', '/uploads/news_cover_003.jpg', '<p>为期一周的\"川剧变脸进校园\"活动在成都市各大中小学圆满结束，近万名学生近距离感受了川剧的独特魅力。</p>', '活动回顾', '教育局', NULL, 654, 189, 1, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_price` decimal(10, 2) NOT NULL COMMENT '订单总价',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货地址',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人电话',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `ship_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (1, 'ORD1775730852214fa1b', 1, 88.00, '测试地址', '非遗爱好者', '13800001001', '测试备注', 1, '2026-04-09 18:34:12', NULL, NULL, NULL, '2026-04-09 18:34:12', '2026-04-09 18:34:12');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（冗余）',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品封面（冗余）',
  `price` decimal(10, 2) NOT NULL COMMENT '商品单价',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
  `subtotal` decimal(10, 2) NOT NULL COMMENT '小计金额',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 1, 7, '川剧脸谱摆件', '/uploads/product_002.jpg', 88.00, 1, 88.00, '2026-04-09 18:34:12');

-- ----------------------------
-- Table structure for performance
-- ----------------------------
DROP TABLE IF EXISTS `performance`;
CREATE TABLE `performance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '演出ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '演出名称',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '演出简介',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '演出地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `organizer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主办方',
  `performer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表演者',
  `price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '票价',
  `seats` int NULL DEFAULT 0 COMMENT '座位数',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '非遗演出表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of performance
-- ----------------------------
INSERT INTO `performance` VALUES (1, '蜀绣技艺展示', '/uploads/perf_cover_001.jpg', '展示蜀绣精湛技艺，现场互动体验', '成都锦里古街', '2026-05-01 14:00:00', NULL, '成都市非遗保护中心', '张大师', 0.00, 100, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `performance` VALUES (2, '川剧变脸专场', '/uploads/perf_cover_002.jpg', '经典川剧变脸表演，感受戏剧魅力', '成都锦江剧场', '2026-05-15 19:30:00', NULL, '四川省川剧院', '王老师', 180.00, 200, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `performance` VALUES (3, '竹编技艺体验', '/uploads/perf_cover_003.jpg', '竹编技艺现场教学，亲手制作竹编作品', '崇州竹艺村', '2026-06-01 10:00:00', NULL, '崇州文旅局', '李师傅', 50.00, 50, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `performance` VALUES (4, '蜀绣技艺展示', '/uploads/perf_cover_001.jpg', '展示蜀绣精湛技艺，现场互动体验', '成都锦里古街', '2026-05-01 14:00:00', NULL, '成都市非遗保护中心', '张大师', 0.00, 100, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `performance` VALUES (5, '川剧变脸专场', '/uploads/perf_cover_002.jpg', '经典川剧变脸表演，感受戏剧魅力', '成都锦江剧场', '2026-05-15 19:30:00', NULL, '四川省川剧院', '王老师', 180.00, 200, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `performance` VALUES (6, '竹编技艺体验', '/uploads/perf_cover_003.jpg', '竹编技艺现场教学，亲手制作竹编作品', '崇州竹艺村', '2026-06-01 10:00:00', NULL, '崇州文旅局', '李师傅', 50.00, 50, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '发帖用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名（冗余）',
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像（冗余）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片（JSON数组）',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `views` int NULL DEFAULT 0 COMMENT '浏览量',
  `likes` int NULL DEFAULT 0 COMMENT '点赞数',
  `comments` int NULL DEFAULT 0 COMMENT '评论数',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-待审核，1-已发布，2-已删除',
  `is_top` tinyint NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `is_essence` tinyint NULL DEFAULT 0 COMMENT '是否精华：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发帖时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区帖子表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post
-- ----------------------------
INSERT INTO `post` VALUES (1, 1, '非遗爱好者', NULL, '第一次学习蜀绣的感受', '今天参加了蜀绣体验活动，第一次拿针，虽然很笨拙，但感受到了传统技艺的魅力。', NULL, '技艺交流', 256, 45, 12, 1, 0, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `post` VALUES (2, 2, '文化守护者', NULL, '推荐几个成都非遗体验地点', '给大家推荐几个成都周边适合体验非遗的地方：锦里、宽窄巷子、崇州竹艺村等。', NULL, '经验分享', 189, 38, 8, 1, 0, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `post` VALUES (3, 1, '非遗爱好者', NULL, '第一次学习蜀绣的感受', '今天参加了蜀绣体验活动，第一次拿针，虽然很笨拙，但感受到了传统技艺的魅力。', NULL, '技艺交流', 256, 45, 12, 1, 0, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `post` VALUES (4, 2, '文化守护者', NULL, '推荐几个成都非遗体验地点', '给大家推荐几个成都周边适合体验非遗的地方：锦里、宽窄巷子、崇州竹艺村等。', NULL, '经验分享', 189, 38, 8, 1, 0, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `post` VALUES (5, 1, '非遗爱好者', NULL, '测试', '测试内容', 'http://localhost:8080/uploads/eab8ee7c-112a-437b-ba58-1eb3239063d8.jpg', '经验分享', 0, 0, 0, 1, 0, 0, '2026-04-09 19:34:07', '2026-04-09 19:34:07');

-- ----------------------------
-- Table structure for post_like
-- ----------------------------
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_post_user`(`post_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_post_id`(`post_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '帖子点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of post_like
-- ----------------------------

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品图片（JSON数组）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `stock` int NULL DEFAULT 0 COMMENT '库存',
  `sales` int NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `is_recommend` tinyint NULL DEFAULT 0 COMMENT '是否推荐：0-否，1-是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, '蜀绣围巾', '/uploads/product_001.jpg', NULL, '精选蚕丝，手工刺绣，图案精美，是馈赠佳品', 1, 298.00, 398.00, 50, 23, 1, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `product` VALUES (2, '川剧脸谱摆件', '/uploads/product_002.jpg', NULL, '树脂材质，手工彩绘，具有收藏价值', 2, 88.00, 128.00, 100, 67, 1, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `product` VALUES (3, '竹编茶具套装', '/uploads/product_003.jpg', NULL, '天然竹材，传统工艺，环保健康', 1, 168.00, 228.00, 30, 15, 1, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `product` VALUES (4, '蜀绣屏风', '/uploads/product_004.jpg', NULL, '双面绣工艺，居家装饰首选', 1, 1280.00, 1680.00, 10, 5, 1, 1, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `product` VALUES (5, '非遗文化T恤', '/uploads/product_005.jpg', NULL, '棉质面料，图案设计融入非遗元素', 3, 79.00, 99.00, 200, 89, 1, 0, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `product` VALUES (6, '蜀绣围巾', '/uploads/product_001.jpg', NULL, '精选蚕丝，手工刺绣，图案精美，是馈赠佳品', 1, 298.00, 398.00, 50, 23, 1, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `product` VALUES (7, '川剧脸谱摆件', '/uploads/product_002.jpg', NULL, '树脂材质，手工彩绘，具有收藏价值', 2, 88.00, 128.00, 99, 68, 1, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `product` VALUES (8, '竹编茶具套装', '/uploads/product_003.jpg', NULL, '天然竹材，传统工艺，环保健康', 1, 168.00, 228.00, 30, 15, 1, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `product` VALUES (9, '蜀绣屏风', '/uploads/product_004.jpg', NULL, '双面绣工艺，居家装饰首选', 1, 1280.00, 1680.00, 10, 5, 1, 1, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `product` VALUES (10, '非遗文化T恤', '/uploads/product_005.jpg', NULL, '棉质面料，图案设计融入非遗元素', 3, 79.00, 99.00, 200, 89, 1, 0, '2026-04-08 22:08:12', '2026-04-08 22:08:12');
INSERT INTO `product` VALUES (11, '测试', '/uploads/22e008b6-5ad1-404c-ad77-396377670c65.jpg', NULL, '测试', 2, 3.00, 4.00, 3, 4, 1, 0, '2026-04-09 13:34:44', '2026-04-09 13:34:44');

-- ----------------------------
-- Table structure for signup
-- ----------------------------
DROP TABLE IF EXISTS `signup`;
CREATE TABLE `signup`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报名人姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝，3-已取消',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_activity_user`(`activity_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of signup
-- ----------------------------
INSERT INTO `signup` VALUES (1, 1, 1, '非遗爱好者', '13800001001', NULL, 1, NULL, NULL, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `signup` VALUES (2, 1, 2, '文化守护者', '13800001002', NULL, 1, NULL, NULL, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `signup` VALUES (3, 2, 1, '非遗爱好者', '13800001001', NULL, 1, NULL, NULL, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `signup` VALUES (4, 2, 2, '文化守护者', '13800001002', NULL, 1, NULL, NULL, '2026-04-07 02:24:15', '2026-04-07 02:24:15');
INSERT INTO `signup` VALUES (5, 3, 1, '非遗爱好者', '13800001001', NULL, 0, NULL, NULL, '2026-04-07 02:24:15', '2026-04-07 02:24:15');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `gender` tinyint NULL DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '普通用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'user001', 'e10adc3949ba59abbe56e057f20f883e', '13800001001', NULL, NULL, '非遗爱好者', 1, NULL, 1, '2026-04-09 20:15:38', '0:0:0:0:0:0:0:1', '2026-04-07 02:24:15', '2026-04-08 22:08:12');
INSERT INTO `user` VALUES (2, 'user002', 'e10adc3949ba59abbe56e057f20f883e', '13800001002', NULL, NULL, '文化守护者', 2, NULL, 1, NULL, NULL, '2026-04-07 02:24:15', '2026-04-08 22:08:12');
INSERT INTO `user` VALUES (3, 'user003', 'e10adc3949ba59abbe56e057f20f883e', '13800001003', NULL, NULL, '手艺传承人', 1, NULL, 1, NULL, NULL, '2026-04-07 02:24:15', '2026-04-08 22:08:12');

SET FOREIGN_KEY_CHECKS = 1;
