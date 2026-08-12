-- Phase 2 business ecosystem: product system, wellness/folk-performance
-- services, service booking and B-side cooperation application.
-- Target: MySQL 8.0+ after V002__phase1_home_foundation.sql
--
-- Forward-only, versioned migration. It keeps every legacy column/table from
-- the ICHIP baseline and the previous phase intact. Do not run it more than
-- once outside a migration runner.
--
-- Compatibility rules applied here:
--   * The legacy `category` / `product.category_id` dimension is NOT touched.
--   * The new `product_system` dimension is stored separately.
--   * Timestamps follow the existing project convention `create_time`/`update_time`.

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 1. Product / service system (new commercial dimension, kept separate from
--    the legacy heritage `category`).
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `product_system` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品体系ID',
  `name` VARCHAR(50) NOT NULL COMMENT '产品体系名称',
  `code` VARCHAR(50) NOT NULL COMMENT '稳定编码',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '产品体系说明',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序，数值越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_system_code` (`code`),
  KEY `idx_product_system_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品/服务体系';

INSERT INTO `product_system` (`name`, `code`, `description`, `sort`, `status`)
SELECT '文创雅物', 'cultural_creative', '文化创意与非遗衍生商品', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `product_system` WHERE `code` = 'cultural_creative');

INSERT INTO `product_system` (`name`, `code`, `description`, `sort`, `status`)
SELECT '美食风物', 'food_culture', '非遗美食与地方风物', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM `product_system` WHERE `code` = 'food_culture');

INSERT INTO `product_system` (`name`, `code`, `description`, `sort`, `status`)
SELECT '器具器物', 'utensils', '传统器具与手作器物', 3, 1
WHERE NOT EXISTS (SELECT 1 FROM `product_system` WHERE `code` = 'utensils');

INSERT INTO `product_system` (`name`, `code`, `description`, `sort`, `status`)
SELECT '手作体验', 'handicraft_experience', '手作课程与现场体验', 4, 1
WHERE NOT EXISTS (SELECT 1 FROM `product_system` WHERE `code` = 'handicraft_experience');

INSERT INTO `product_system` (`name`, `code`, `description`, `sort`, `status`)
SELECT '康养陪伴', 'wellness', '康养陪伴与身心健康服务', 5, 1
WHERE NOT EXISTS (SELECT 1 FROM `product_system` WHERE `code` = 'wellness');

INSERT INTO `product_system` (`name`, `code`, `description`, `sort`, `status`)
SELECT '民俗演艺', 'folk_performance', '民俗节庆与演艺服务', 6, 1
WHERE NOT EXISTS (SELECT 1 FROM `product_system` WHERE `code` = 'folk_performance');

-- ---------------------------------------------------------------------------
-- 2. Business service (wellness companion / folk performance, extendable to
--    any product system). Kept independent from the legacy `course` table.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `business_service` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务ID',
  `name` VARCHAR(100) NOT NULL COMMENT '服务名称',
  `product_system_id` BIGINT NOT NULL COMMENT '产品体系ID',
  `cover` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
  `images` TEXT COMMENT '详情图片（JSON 或逗号分隔）',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '服务摘要',
  `description` TEXT COMMENT '服务介绍',
  `provider_name` VARCHAR(200) DEFAULT NULL COMMENT '服务提供方名称',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '服务地点',
  `price` DECIMAL(10,2) DEFAULT NULL COMMENT '展示价格',
  `unit` VARCHAR(50) DEFAULT NULL COMMENT '计价单位，如 人/次',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-启用',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序，数值越小越靠前',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business_service_system` (`product_system_id`),
  KEY `idx_business_service_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='康养/演艺等服务';

-- ---------------------------------------------------------------------------
-- 3. Service schedule (phase-1 basic sessions only, no complex scheduling).
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `business_service_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '场次ID',
  `service_id` BIGINT NOT NULL COMMENT '服务ID',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `capacity` INT NOT NULL DEFAULT 0 COMMENT '场次容量，0 表示不限',
  `booked_count` INT NOT NULL DEFAULT 0 COMMENT '已预约人数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-停用，1-可预约，2-已结束',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business_schedule_service_time` (`service_id`, `status`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务场次';

-- ---------------------------------------------------------------------------
-- 4. Service booking (minimal booking loop, no real payment).
--    status: 1-已预约, 2-已取消, 3-已完成
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `business_service_booking` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `service_id` BIGINT NOT NULL COMMENT '服务ID',
  `schedule_id` BIGINT NOT NULL COMMENT '场次ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '预约人数',
  `contact_name` VARCHAR(100) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-已预约，2-已取消，3-已完成',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_business_booking_user` (`user_id`, `status`),
  KEY `idx_business_booking_schedule` (`schedule_id`),
  KEY `idx_business_booking_service` (`service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务预约';

-- ---------------------------------------------------------------------------
-- 5. B-side cooperation application.
--    status: 0-待处理, 1-已联系, 2-已完成, 3-已关闭
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `cooperation_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `company_name` VARCHAR(200) NOT NULL COMMENT '企业/机构名称',
  `contact_name` VARCHAR(100) NOT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(30) NOT NULL COMMENT '联系电话',
  `cooperation_type` VARCHAR(50) NOT NULL COMMENT '合作类型编码',
  `requirement` TEXT COMMENT '合作需求说明',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-已联系，2-已完成，3-已关闭',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '后台备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_cooperation_application_status` (`status`),
  KEY `idx_cooperation_application_type` (`cooperation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='B端合作申请';

-- ---------------------------------------------------------------------------
-- 6. Link existing business tables to the product system. The legacy
--    `category` / `category_id` dimension is left untouched.
-- ---------------------------------------------------------------------------

DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;
DELIMITER $$
CREATE PROCEDURE `add_column_if_not_exists`(IN tbl VARCHAR(64), IN col VARCHAR(64), IN ddl VARCHAR(512))
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM `information_schema`.`COLUMNS`
    WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = tbl AND `COLUMN_NAME` = col
  ) THEN
    SET @ddl_stmt = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN ', ddl);
    PREPARE stmt FROM @ddl_stmt;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL `add_column_if_not_exists`(
  'product', 'product_system_id',
  '`product_system_id` BIGINT DEFAULT NULL COMMENT ''产品体系ID（新版六大产品体系）'' AFTER `category_id`'
);
DROP PROCEDURE IF EXISTS `add_index_if_not_exists`;
DELIMITER $$
CREATE PROCEDURE `add_index_if_not_exists`(IN tbl VARCHAR(64), IN idx VARCHAR(64), IN ddl VARCHAR(512))
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM `information_schema`.`STATISTICS`
    WHERE `TABLE_SCHEMA` = DATABASE() AND `TABLE_NAME` = tbl AND `INDEX_NAME` = idx
  ) THEN
    SET @ddl_stmt = CONCAT('ALTER TABLE `', tbl, '` ADD INDEX ', ddl);
    PREPARE stmt FROM @ddl_stmt;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL `add_index_if_not_exists`('product', 'idx_product_system_id', '`idx_product_system_id` (`product_system_id`)');
CALL `add_column_if_not_exists`(
  'activity', 'product_system_id',
  '`product_system_id` BIGINT DEFAULT NULL COMMENT ''产品体系ID（新版六大产品体系）'''
);
CALL `add_index_if_not_exists`('activity', 'idx_activity_product_system_id', '`idx_activity_product_system_id` (`product_system_id`)');

DROP PROCEDURE IF EXISTS `add_index_if_not_exists`;
DROP PROCEDURE IF EXISTS `add_column_if_not_exists`;
