-- Phase 1 home page: minimum database foundation
-- Target: MySQL 8.0+
--
-- This is a forward-only, versioned migration. It deliberately keeps every
-- legacy column and row used by the ICHIP baseline. Do not run it more than
-- once outside a migration runner.

SET NAMES utf8mb4;

-- 1. City dictionary. Only the current home-page default city is seeded.
CREATE TABLE IF NOT EXISTS `city` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '城市ID',
  `code` VARCHAR(20) NOT NULL COMMENT '行政区划代码',
  `name` VARCHAR(100) NOT NULL COMMENT '城市名称',
  `province_code` VARCHAR(20) DEFAULT NULL COMMENT '省级行政区划代码',
  `province_name` VARCHAR(100) DEFAULT NULL COMMENT '省份名称',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认城市：0-否，1-是',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序，数值越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_city_code` (`code`),
  KEY `idx_city_status_sort` (`status`, `sort`),
  KEY `idx_city_default` (`is_default`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台城市字典';

INSERT INTO `city`
  (`code`, `name`, `province_code`, `province_name`, `is_default`, `sort`, `status`)
SELECT
  '320200', '无锡市', '320000', '江苏省', 1, 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `city` WHERE `code` = '320200'
);

-- 2. Reuse category for multiple business domains while preserving all
-- existing product categories.
ALTER TABLE `category`
  ADD COLUMN `code` VARCHAR(64) DEFAULT NULL COMMENT '业务内稳定编码',
  ADD COLUMN `biz_type` VARCHAR(32) NOT NULL DEFAULT 'PRODUCT' COMMENT '业务类型：HERITAGE/PRODUCT/KNOWLEDGE/COURSE',
  ADD COLUMN `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
  ADD COLUMN `description` VARCHAR(500) DEFAULT NULL COMMENT '分类说明',
  ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  ADD UNIQUE KEY `uk_category_biz_code` (`biz_type`, `code`),
  ADD KEY `idx_category_biz_status_sort` (`biz_type`, `status`, `sort`);

UPDATE `category`
SET `biz_type` = 'PRODUCT'
WHERE `biz_type` IS NULL OR `biz_type` = '';

INSERT INTO `category` (`name`, `code`, `biz_type`, `parent_id`, `sort`, `status`)
SELECT '传统美术', 'TRADITIONAL_ART', 'HERITAGE', 0, 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'HERITAGE' AND `code` = 'TRADITIONAL_ART'
);

INSERT INTO `category` (`name`, `code`, `biz_type`, `parent_id`, `sort`, `status`)
SELECT '传统技艺', 'TRADITIONAL_CRAFT', 'HERITAGE', 0, 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'HERITAGE' AND `code` = 'TRADITIONAL_CRAFT'
);

INSERT INTO `category` (`name`, `code`, `biz_type`, `parent_id`, `sort`, `status`)
SELECT '传统民俗', 'TRADITIONAL_FOLKLORE', 'HERITAGE', 0, 3, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'HERITAGE' AND `code` = 'TRADITIONAL_FOLKLORE'
);

INSERT INTO `category` (`name`, `code`, `biz_type`, `parent_id`, `sort`, `status`)
SELECT '传统医药', 'TRADITIONAL_MEDICINE', 'HERITAGE', 0, 4, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'HERITAGE' AND `code` = 'TRADITIONAL_MEDICINE'
);

INSERT INTO `category` (`name`, `code`, `biz_type`, `parent_id`, `sort`, `status`)
SELECT '传统戏曲', 'TRADITIONAL_OPERA', 'HERITAGE', 0, 5, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'HERITAGE' AND `code` = 'TRADITIONAL_OPERA'
);

INSERT INTO `category` (`name`, `code`, `biz_type`, `parent_id`, `sort`, `status`)
SELECT '非遗美食', 'HERITAGE_FOOD', 'HERITAGE', 0, 6, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'HERITAGE' AND `code` = 'HERITAGE_FOOD'
);

-- 3. Controlled heritage levels used by the authoritative project section.
CREATE TABLE IF NOT EXISTS `heritage_level` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '级别ID',
  `code` VARCHAR(32) NOT NULL COMMENT '稳定编码',
  `name` VARCHAR(50) NOT NULL COMMENT '级别名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序，数值越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_heritage_level_code` (`code`),
  KEY `idx_heritage_level_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='非遗级别字典';

INSERT INTO `heritage_level` (`code`, `name`, `sort`, `status`)
SELECT 'NATIONAL', '国家级', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `heritage_level` WHERE `code` = 'NATIONAL');

INSERT INTO `heritage_level` (`code`, `name`, `sort`, `status`)
SELECT 'PROVINCIAL', '省级', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM `heritage_level` WHERE `code` = 'PROVINCIAL');

INSERT INTO `heritage_level` (`code`, `name`, `sort`, `status`)
SELECT 'MUNICIPAL', '市级', 3, 1
WHERE NOT EXISTS (SELECT 1 FROM `heritage_level` WHERE `code` = 'MUNICIPAL');

INSERT INTO `heritage_level` (`code`, `name`, `sort`, `status`)
SELECT 'DISTRICT', '区级', 4, 1
WHERE NOT EXISTS (SELECT 1 FROM `heritage_level` WHERE `code` = 'DISTRICT');

-- 4. Extend the existing heritage project table. Legacy text columns stay in
-- place so current pages and test data continue to work during migration.
ALTER TABLE `heritage_project`
  ADD COLUMN `official_code` VARCHAR(100) DEFAULT NULL COMMENT '官方名录编号',
  ADD COLUMN `category_id` BIGINT DEFAULT NULL COMMENT '规范非遗分类ID',
  ADD COLUMN `level_code` VARCHAR(32) DEFAULT NULL COMMENT '规范非遗级别编码',
  ADD COLUMN `region_code` VARCHAR(32) DEFAULT NULL COMMENT '行政区划编码',
  ADD COLUMN `recognition_authority` VARCHAR(200) DEFAULT NULL COMMENT '认定机构',
  ADD COLUMN `recognition_batch` VARCHAR(100) DEFAULT NULL COMMENT '认定批次',
  ADD COLUMN `recognized_at` DATE DEFAULT NULL COMMENT '认定日期',
  ADD COLUMN `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否首页推荐：0-否，1-是',
  ADD COLUMN `recommend_sort` INT NOT NULL DEFAULT 0 COMMENT '首页推荐排序',
  ADD UNIQUE KEY `uk_heritage_project_official_code` (`official_code`),
  ADD KEY `idx_heritage_project_filter` (`category_id`, `level_code`, `region_code`),
  ADD KEY `idx_heritage_project_home` (`status`, `is_recommend`, `recommend_sort`);

UPDATE `heritage_project` AS hp
JOIN `category` AS c
  ON c.`biz_type` = 'HERITAGE'
 AND c.`code` = CASE hp.`category`
   WHEN '传统美术' THEN 'TRADITIONAL_ART'
   WHEN '传统技艺' THEN 'TRADITIONAL_CRAFT'
   WHEN '传统民俗' THEN 'TRADITIONAL_FOLKLORE'
   WHEN '传统医药' THEN 'TRADITIONAL_MEDICINE'
   WHEN '传统戏曲' THEN 'TRADITIONAL_OPERA'
   WHEN '传统戏剧' THEN 'TRADITIONAL_OPERA'
   WHEN '非遗美食' THEN 'HERITAGE_FOOD'
   ELSE NULL
 END
SET hp.`category_id` = c.`id`
WHERE hp.`category_id` IS NULL;

UPDATE `heritage_project`
SET `level_code` = CASE `level`
      WHEN '国家级' THEN 'NATIONAL'
      WHEN '省级' THEN 'PROVINCIAL'
      WHEN '市级' THEN 'MUNICIPAL'
      WHEN '区级' THEN 'DISTRICT'
      WHEN '县级' THEN 'DISTRICT'
      WHEN '区县级' THEN 'DISTRICT'
      ELSE `level_code`
    END,
    `is_recommend` = CASE WHEN `status` = 1 THEN 1 ELSE 0 END,
    `recommend_sort` = COALESCE(`sort`, 0);

-- 5. Add a public display boundary for inheritors. Sensitive legacy columns
-- remain untouched and must never be selected into future public DTOs.
ALTER TABLE `inheritor`
  ADD COLUMN `display_name` VARCHAR(100) DEFAULT NULL COMMENT '公开展示名称',
  ADD COLUMN `portrait` VARCHAR(255) DEFAULT NULL COMMENT '公开头像',
  ADD COLUMN `region_code` VARCHAR(32) DEFAULT NULL COMMENT '行政区划编码',
  ADD COLUMN `profile` TEXT COMMENT '公开简介',
  ADD COLUMN `display_status` TINYINT NOT NULL DEFAULT 0 COMMENT '公开展示状态：0-隐藏，1-展示',
  ADD COLUMN `published_at` DATETIME DEFAULT NULL COMMENT '公开发布时间',
  ADD COLUMN `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否首页推荐：0-否，1-是',
  ADD COLUMN `recommend_sort` INT NOT NULL DEFAULT 0 COMMENT '首页推荐排序',
  ADD KEY `idx_inheritor_public_home`
    (`audit_status`, `status`, `display_status`, `is_recommend`, `recommend_sort`),
  ADD KEY `idx_inheritor_region` (`region_code`);

UPDATE `inheritor`
SET `display_name` = COALESCE(NULLIF(`display_name`, ''), `name`),
    `profile` = COALESCE(`profile`, `skill_desc`),
    `display_status` = CASE
      WHEN `audit_status` = 1 AND `status` = 1 THEN 1
      ELSE 0
    END,
    `published_at` = CASE
      WHEN `audit_status` = 1 AND `status` = 1
        THEN COALESCE(`published_at`, `create_time`)
      ELSE `published_at`
    END,
    `is_recommend` = CASE
      WHEN `audit_status` = 1 AND `status` = 1 THEN 1
      ELSE 0
    END;

-- 6. Add city and recommendation metadata to existing activities. Legacy
-- locations are copied only to address_detail; city_code is intentionally not
-- guessed so Chengdu test records are not mislabeled as Wuxi.
ALTER TABLE `activity`
  ADD COLUMN `city_code` VARCHAR(32) DEFAULT NULL COMMENT '活动城市编码',
  ADD COLUMN `address_detail` VARCHAR(255) DEFAULT NULL COMMENT '活动详细地址',
  ADD COLUMN `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否首页推荐：0-否，1-是',
  ADD COLUMN `recommend_sort` INT NOT NULL DEFAULT 0 COMMENT '首页推荐排序',
  ADD KEY `idx_activity_home`
    (`status`, `city_code`, `is_recommend`, `start_time`),
  ADD KEY `idx_activity_recommend_sort` (`recommend_sort`);

UPDATE `activity`
SET `address_detail` = COALESCE(`address_detail`, `location`),
    `is_recommend` = CASE WHEN `status` = 1 THEN 1 ELSE 0 END;

-- 7. Minimum course and session model required for the home-page weekly
-- course section. Reservation, payment, rescheduling and verification are out
-- of scope for this migration.
CREATE TABLE IF NOT EXISTS `course` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `title` VARCHAR(200) NOT NULL COMMENT '课程标题',
  `cover` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '课程摘要',
  `description` TEXT COMMENT '课程介绍',
  `inheritor_id` BIGINT DEFAULT NULL COMMENT '授课传承人ID',
  `heritage_project_id` BIGINT DEFAULT NULL COMMENT '关联非遗项目ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '课程分类ID',
  `service_mode` VARCHAR(32) NOT NULL DEFAULT 'OFFLINE' COMMENT '服务模式',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '展示价格',
  `duration_minutes` INT DEFAULT NULL COMMENT '课程时长（分钟）',
  `city_code` VARCHAR(32) DEFAULT NULL COMMENT '默认城市编码',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '默认授课地点',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-拒绝',
  `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态：0-未发布，1-已发布',
  `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT '是否首页推荐：0-否，1-是',
  `recommend_sort` INT NOT NULL DEFAULT 0 COMMENT '首页推荐排序',
  `published_at` DATETIME DEFAULT NULL COMMENT '发布时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_inheritor` (`inheritor_id`),
  KEY `idx_course_project` (`heritage_project_id`),
  KEY `idx_course_category` (`category_id`),
  KEY `idx_course_home`
    (`publish_status`, `city_code`, `is_recommend`, `recommend_sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手作课程';

CREATE TABLE IF NOT EXISTS `course_session` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程场次ID',
  `course_id` BIGINT NOT NULL COMMENT '课程ID',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `city_code` VARCHAR(32) DEFAULT NULL COMMENT '场次城市编码',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '场次地点',
  `capacity` INT NOT NULL DEFAULT 0 COMMENT '场次容量',
  `booked_count` INT NOT NULL DEFAULT 0 COMMENT '已占名额，仅作后续预约模块预留',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-可展示，2-已结束',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_course_session_course_time` (`course_id`, `status`, `start_time`),
  KEY `idx_course_session_city_time` (`city_code`, `status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='手作课程场次';

-- 8. Existing recommendation flags are sufficient for products and news;
-- only add indexes needed by the home-page read queries.
ALTER TABLE `product`
  ADD KEY `idx_product_home`
    (`status`, `is_recommend`, `sales`, `create_time`);

ALTER TABLE `news`
  ADD KEY `idx_news_home`
    (`status`, `is_top`, `create_time`);

