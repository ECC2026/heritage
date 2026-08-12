-- Phase 2 business ecosystem development seed data
-- Target: MySQL 8.0+ after V003__business_ecosystem.sql
--
-- All records prefixed with "开发测试" / "DEV-TEST" are fictional. They must
-- never be interpreted as real operational data. Idempotency is based on the
-- stable service name so repeat execution does not duplicate rows.

SET NAMES utf8mb4;
START TRANSACTION;

INSERT INTO `business_service`
  (`name`, `product_system_id`, `cover`, `summary`, `description`, `provider_name`,
   `location`, `price`, `unit`, `status`, `sort`)
SELECT
  '开发测试·非遗康养陪伴服务', ps.`id`, '/static/img/logo1.jpg',
  '虚构开发测试服务：面向老年群体的非遗康养陪伴。',
  '仅用于开发环境，服务提供方、场地和时段均不代表真实经营信息。',
  '开发测试·康养服务机构', '开发测试·无锡康养体验中心（虚构地址）',
  68.00, '人/次', 1, 1
FROM `product_system` ps
WHERE ps.`code` = 'wellness'
  AND NOT EXISTS (SELECT 1 FROM `business_service` WHERE `name` = '开发测试·非遗康养陪伴服务');

INSERT INTO `business_service`
  (`name`, `product_system_id`, `cover`, `summary`, `description`, `provider_name`,
   `location`, `price`, `unit`, `status`, `sort`)
SELECT
  '开发测试·民俗演艺专场', ps.`id`, '/static/img/lbt3.jpg',
  '虚构开发测试服务：非遗民俗演艺专场演出。',
  '仅用于开发环境，服务提供方、场地和时段均不代表真实经营信息。',
  '开发测试·民俗演艺团', '开发测试·无锡民俗小剧场（虚构地址）',
  128.00, '人/次', 1, 2
FROM `product_system` ps
WHERE ps.`code` = 'folk_performance'
  AND NOT EXISTS (SELECT 1 FROM `business_service` WHERE `name` = '开发测试·民俗演艺专场');

INSERT INTO `business_service`
  (`name`, `product_system_id`, `cover`, `summary`, `description`, `provider_name`,
   `location`, `price`, `unit`, `status`, `sort`)
SELECT
  '开发测试·非遗竹编体验服务', ps.`id`, '/static/img/logo.png',
  '虚构开发测试服务：由传承人指导的竹编手作体验。',
  '仅用于开发环境，服务提供方、场地和时段均不代表真实经营信息。',
  '开发测试·竹编工作室', '开发测试·无锡竹编工坊（虚构地址）',
  88.00, '人/次', 1, 3
FROM `product_system` ps
WHERE ps.`code` = 'handicraft_experience'
  AND NOT EXISTS (SELECT 1 FROM `business_service` WHERE `name` = '开发测试·非遗竹编体验服务');

-- Schedules (phase-1 basic sessions). Anchor times are kept in the future so
-- the C-end can actually book them during development.
SET @phase2_schedule_anchor = DATE_ADD(NOW(), INTERVAL 1 DAY);

INSERT INTO `business_service_schedule`
  (`service_id`, `start_time`, `end_time`, `capacity`, `booked_count`, `status`)
SELECT bs.`id`, @phase2_schedule_anchor, DATE_ADD(@phase2_schedule_anchor, INTERVAL 2 HOUR), 20, 3, 1
FROM `business_service` bs
WHERE bs.`name` = '开发测试·非遗康养陪伴服务'
  AND NOT EXISTS (
    SELECT 1 FROM `business_service_schedule` s
    WHERE s.`service_id` = bs.`id` AND s.`start_time` = @phase2_schedule_anchor
  );

INSERT INTO `business_service_schedule`
  (`service_id`, `start_time`, `end_time`, `capacity`, `booked_count`, `status`)
SELECT bs.`id`, DATE_ADD(@phase2_schedule_anchor, INTERVAL 1 DAY), DATE_ADD(@phase2_schedule_anchor, INTERVAL 3 HOUR), 0, 0, 1
FROM `business_service` bs
WHERE bs.`name` = '开发测试·非遗康养陪伴服务'
  AND NOT EXISTS (
    SELECT 1 FROM `business_service_schedule` s
    WHERE s.`service_id` = bs.`id` AND s.`start_time` = DATE_ADD(@phase2_schedule_anchor, INTERVAL 1 DAY)
  );

INSERT INTO `business_service_schedule`
  (`service_id`, `start_time`, `end_time`, `capacity`, `booked_count`, `status`)
SELECT bs.`id`, @phase2_schedule_anchor, DATE_ADD(@phase2_schedule_anchor, INTERVAL 2 HOUR), 200, 20, 1
FROM `business_service` bs
WHERE bs.`name` = '开发测试·民俗演艺专场'
  AND NOT EXISTS (
    SELECT 1 FROM `business_service_schedule` s
    WHERE s.`service_id` = bs.`id` AND s.`start_time` = @phase2_schedule_anchor
  );

INSERT INTO `business_service_schedule`
  (`service_id`, `start_time`, `end_time`, `capacity`, `booked_count`, `status`)
SELECT bs.`id`, DATE_ADD(@phase2_schedule_anchor, INTERVAL 2 DAY), DATE_ADD(@phase2_schedule_anchor, INTERVAL 2 HOUR), 10, 0, 0
FROM `business_service` bs
WHERE bs.`name` = '开发测试·非遗竹编体验服务'
  AND NOT EXISTS (
    SELECT 1 FROM `business_service_schedule` s
    WHERE s.`service_id` = bs.`id` AND s.`start_time` = DATE_ADD(@phase2_schedule_anchor, INTERVAL 2 DAY)
  );

COMMIT;
