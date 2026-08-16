-- Phase 1 home page development seed data
-- Target: MySQL 8.0+ after V002__phase1_home_foundation.sql
--
-- All people, accounts, contact values, venues, qualification remarks and
-- business records prefixed with "开发测试" / "DEV-TEST" are fictional.
-- They must never be interpreted as real official recognition or identity data.
--
-- Idempotency strategy:
--   * Seed-owned rows use stable codes, usernames, titles or names.
--   * Existing ICHIP rows are updated only when the V002 field is NULL/blank.
--   * Repeat execution refreshes only seed-owned future schedules/activities.

SET NAMES utf8mb4;
START TRANSACTION;

-- ---------------------------------------------------------------------------
-- 1. City and category dictionaries
-- ---------------------------------------------------------------------------

INSERT INTO `city`
  (`code`, `name`, `province_code`, `province_name`, `is_default`, `sort`, `status`)
SELECT '510100', '成都市', '510000', '四川省', 0, 20, 1
WHERE NOT EXISTS (SELECT 1 FROM `city` WHERE `code` = '510100');

INSERT INTO `city`
  (`code`, `name`, `province_code`, `province_name`, `is_default`, `sort`, `status`)
SELECT '510184', '崇州市', '510000', '四川省', 0, 21, 1
WHERE NOT EXISTS (SELECT 1 FROM `city` WHERE `code` = '510184');

INSERT INTO `city`
  (`code`, `name`, `province_code`, `province_name`, `is_default`, `sort`, `status`)
SELECT '513300', '甘孜藏族自治州', '510000', '四川省', 0, 22, 1
WHERE NOT EXISTS (SELECT 1 FROM `city` WHERE `code` = '513300');

UPDATE `category`
SET `code` = CONCAT('LEGACY_PRODUCT_', LPAD(`id`, 6, '0')),
    `description` = COALESCE(NULLIF(`description`, ''), 'ICHIP 原始商品分类的开发兼容编码')
WHERE `biz_type` = 'PRODUCT'
  AND (`code` IS NULL OR TRIM(`code`) = '');

UPDATE `category`
SET `icon` = COALESCE(NULLIF(`icon`, ''), '/static/img/logo.png'),
    `description` = COALESCE(
      NULLIF(`description`, ''),
      CONCAT(`name`, '分类开发测试说明')
    )
WHERE `biz_type` = 'HERITAGE';

INSERT INTO `category`
  (`name`, `code`, `biz_type`, `icon`, `description`, `parent_id`, `sort`, `status`)
SELECT
  '单人体验', 'COURSE_INDIVIDUAL', 'COURSE', '/static/img/logo.png',
  '面向单位学员的开发测试课程分类', 0, 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'COURSE' AND `code` = 'COURSE_INDIVIDUAL'
);

INSERT INTO `category`
  (`name`, `code`, `biz_type`, `icon`, `description`, `parent_id`, `sort`, `status`)
SELECT
  '亲子手作', 'COURSE_FAMILY', 'COURSE', '/static/img/logo.png',
  '面向亲子共同参与的开发测试课程分类', 0, 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'COURSE' AND `code` = 'COURSE_FAMILY'
);

INSERT INTO `category`
  (`name`, `code`, `biz_type`, `icon`, `description`, `parent_id`, `sort`, `status`)
SELECT
  '多人团建', 'COURSE_TEAM', 'COURSE', '/static/img/logo.png',
  '面向多人团队的开发测试课程分类', 0, 3, 1
WHERE NOT EXISTS (
  SELECT 1 FROM `category`
  WHERE `biz_type` = 'COURSE' AND `code` = 'COURSE_TEAM'
);

-- ---------------------------------------------------------------------------
-- 2. Backfill only empty V002 fields on the original ICHIP records
-- ---------------------------------------------------------------------------

UPDATE `heritage_project` AS hp
LEFT JOIN `category` AS hc
       ON hc.`biz_type` = 'HERITAGE'
      AND hc.`code` = CASE hp.`category`
        WHEN '传统美术' THEN 'TRADITIONAL_ART'
        WHEN '传统技艺' THEN 'TRADITIONAL_CRAFT'
        WHEN '传统民俗' THEN 'TRADITIONAL_FOLKLORE'
        WHEN '传统医药' THEN 'TRADITIONAL_MEDICINE'
        WHEN '传统戏曲' THEN 'TRADITIONAL_OPERA'
        WHEN '传统戏剧' THEN 'TRADITIONAL_OPERA'
        WHEN '非遗美食' THEN 'HERITAGE_FOOD'
        ELSE NULL
      END
SET hp.`category_id` = COALESCE(hp.`category_id`, hc.`id`),
    hp.`level_code` = COALESCE(
      NULLIF(hp.`level_code`, ''),
      CASE hp.`level`
        WHEN '国家级' THEN 'NATIONAL'
        WHEN '省级' THEN 'PROVINCIAL'
        WHEN '市级' THEN 'MUNICIPAL'
        WHEN '区级' THEN 'DISTRICT'
        WHEN '县级' THEN 'DISTRICT'
        WHEN '区县级' THEN 'DISTRICT'
        ELSE NULL
      END
    ),
    hp.`region_code` = COALESCE(
      NULLIF(hp.`region_code`, ''),
      CASE hp.`region`
        WHEN '四川省成都市' THEN '510100'
        WHEN '四川省崇州市' THEN '510184'
        WHEN '四川省甘孜州' THEN '513300'
        WHEN '四川省' THEN '510000'
        ELSE NULL
      END
    ),
    hp.`official_code` = COALESCE(
      NULLIF(hp.`official_code`, ''),
      CONCAT('DEV-TEST-HP-', LPAD(hp.`id`, 6, '0'))
    ),
    hp.`recognition_authority` = COALESCE(
      NULLIF(hp.`recognition_authority`, ''),
      '开发测试数据（非官方认定）'
    ),
    hp.`recognition_batch` = COALESCE(
      NULLIF(hp.`recognition_batch`, ''),
      'DEV-TEST 开发测试批次'
    ),
    hp.`recognized_at` = COALESCE(
      hp.`recognized_at`,
      DATE_ADD('2020-01-01', INTERVAL hp.`id` DAY)
    )
WHERE hp.`name` IN ('蜀绣', '川剧变脸', '竹编技艺', '银花丝', '藏族编织')
  AND hp.`region` IN ('四川省成都市', '四川省崇州市', '四川省甘孜州', '四川省');

UPDATE `inheritor`
SET `portrait` = COALESCE(NULLIF(`portrait`, ''), '/static/img/logo.png'),
    `region_code` = COALESCE(NULLIF(`region_code`, ''), '510100'),
    `profile` = COALESCE(NULLIF(`profile`, ''), `skill_desc`),
    `recommend_sort` = CASE
      WHEN `recommend_sort` = 0 THEN `id`
      ELSE `recommend_sort`
    END
WHERE `name` = '张大师'
  AND `skill_type` = '蜀绣';

UPDATE `activity`
SET `city_code` = CASE
      WHEN (`city_code` IS NULL OR TRIM(`city_code`) = '')
           AND `location` LIKE '%崇州%' THEN '510184'
      WHEN (`city_code` IS NULL OR TRIM(`city_code`) = '')
           AND (`location` LIKE '%成都%' OR `location` LIKE '%锦江%') THEN '510100'
      ELSE `city_code`
    END,
    `address_detail` = COALESCE(NULLIF(`address_detail`, ''), `location`)
WHERE `location` LIKE '%成都%'
   OR `location` LIKE '%锦江%'
   OR `location` LIKE '%崇州%';

-- ---------------------------------------------------------------------------
-- 3. Fictional disabled user accounts required by inheritor.user_id
-- ---------------------------------------------------------------------------

INSERT INTO `user`
  (`username`, `password`, `phone`, `email`, `avatar`, `nickname`, `gender`, `status`)
SELECT
  'DEV_SEED_INHERITOR_001', '!DEV_TEST_ACCOUNT_DISABLED!', '00000000001',
  'dev-inheritor-001@example.invalid', '/static/img/logo.png', '开发测试·林师傅', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'DEV_SEED_INHERITOR_001');

INSERT INTO `user`
  (`username`, `password`, `phone`, `email`, `avatar`, `nickname`, `gender`, `status`)
SELECT
  'DEV_SEED_INHERITOR_002', '!DEV_TEST_ACCOUNT_DISABLED!', '00000000002',
  'dev-inheritor-002@example.invalid', '/static/img/logo.png', '开发测试·周老师', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'DEV_SEED_INHERITOR_002');

INSERT INTO `user`
  (`username`, `password`, `phone`, `email`, `avatar`, `nickname`, `gender`, `status`)
SELECT
  'DEV_SEED_INHERITOR_003', '!DEV_TEST_ACCOUNT_DISABLED!', '00000000003',
  'dev-inheritor-003@example.invalid', '/static/img/logo.png', '开发测试·陈教习', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'DEV_SEED_INHERITOR_003');

INSERT INTO `user`
  (`username`, `password`, `phone`, `email`, `avatar`, `nickname`, `gender`, `status`)
SELECT
  'DEV_SEED_INHERITOR_004', '!DEV_TEST_ACCOUNT_DISABLED!', '00000000004',
  'dev-inheritor-004@example.invalid', '/static/img/logo.png', '开发测试·吴匠人', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'DEV_SEED_INHERITOR_004');

-- ---------------------------------------------------------------------------
-- 4. Fictional inheritor profiles with review/display state coverage
-- ---------------------------------------------------------------------------

INSERT INTO `inheritor`
  (`user_id`, `name`, `phone`, `id_card`, `skill_type`, `skill_desc`, `experience`,
   `certificate`, `作品展示`, `audit_status`, `audit_remark`, `audit_time`, `status`,
   `display_name`, `portrait`, `region_code`, `profile`, `display_status`,
   `published_at`, `is_recommend`, `recommend_sort`)
SELECT
  u.`id`, '开发测试·林师傅', '00000000001', NULL, '锡绣',
  '虚构开发测试人物，用于演示单人刺绣体验课程。',
  '开发测试经历，不对应任何真实人物。', NULL, '/static/img/logo1.jpg',
  1, '开发测试审核通过；无真实证书或官方认证。', NOW(), 1,
  '开发测试·林师傅', '/static/img/logo.png', '320200',
  '虚构开发测试传承人，展示锡绣基础针法与纹样体验。', 1, NOW(), 1, 101
FROM `user` u
WHERE u.`username` = 'DEV_SEED_INHERITOR_001'
  AND NOT EXISTS (
    SELECT 1 FROM `inheritor` WHERE `display_name` = '开发测试·林师傅'
  );

INSERT INTO `inheritor`
  (`user_id`, `name`, `phone`, `id_card`, `skill_type`, `skill_desc`, `experience`,
   `certificate`, `作品展示`, `audit_status`, `audit_remark`, `audit_time`, `status`,
   `display_name`, `portrait`, `region_code`, `profile`, `display_status`,
   `published_at`, `is_recommend`, `recommend_sort`)
SELECT
  u.`id`, '开发测试·周老师', '00000000002', NULL, '泥塑',
  '虚构开发测试人物，用于演示亲子泥塑与上门手作课程。',
  '开发测试经历，不对应任何真实人物。', NULL, '/static/img/logo1.jpg',
  1, '开发测试审核通过；无真实证书或官方认证。', NOW(), 1,
  '开发测试·周老师', '/static/img/logo.png', '320200',
  '虚构开发测试传承人，展示亲子泥塑和传统造型体验。', 1, NOW(), 1, 102
FROM `user` u
WHERE u.`username` = 'DEV_SEED_INHERITOR_002'
  AND NOT EXISTS (
    SELECT 1 FROM `inheritor` WHERE `display_name` = '开发测试·周老师'
  );

INSERT INTO `inheritor`
  (`user_id`, `name`, `phone`, `id_card`, `skill_type`, `skill_desc`, `experience`,
   `certificate`, `作品展示`, `audit_status`, `audit_remark`, `audit_time`, `status`,
   `display_name`, `portrait`, `region_code`, `profile`, `display_status`,
   `published_at`, `is_recommend`, `recommend_sort`)
SELECT
  u.`id`, '开发测试·陈教习', '00000000003', NULL, '竹编',
  '虚构开发测试人物，用于覆盖待审核状态。',
  '开发测试经历，不对应任何真实人物。', NULL, '/static/img/logo1.jpg',
  0, '开发测试待审核记录；无真实证书或官方认证。', NULL, 1,
  '开发测试·陈教习', '/static/img/logo.png', '320200',
  '虚构开发测试传承人，当前处于待审核隐藏状态。', 0, NULL, 0, 103
FROM `user` u
WHERE u.`username` = 'DEV_SEED_INHERITOR_003'
  AND NOT EXISTS (
    SELECT 1 FROM `inheritor` WHERE `display_name` = '开发测试·陈教习'
  );

INSERT INTO `inheritor`
  (`user_id`, `name`, `phone`, `id_card`, `skill_type`, `skill_desc`, `experience`,
   `certificate`, `作品展示`, `audit_status`, `audit_remark`, `audit_time`, `status`,
   `display_name`, `portrait`, `region_code`, `profile`, `display_status`,
   `published_at`, `is_recommend`, `recommend_sort`)
SELECT
  u.`id`, '开发测试·吴匠人', '00000000004', NULL, '剪纸',
  '虚构开发测试人物，用于覆盖审核未通过状态。',
  '开发测试经历，不对应任何真实人物。', NULL, '/static/img/logo1.jpg',
  2, '开发测试审核未通过记录；无真实证书或官方认证。', NOW(), 1,
  '开发测试·吴匠人', '/static/img/logo.png', '320200',
  '虚构开发测试传承人，当前处于审核未通过隐藏状态。', 0, NULL, 0, 104
FROM `user` u
WHERE u.`username` = 'DEV_SEED_INHERITOR_004'
  AND NOT EXISTS (
    SELECT 1 FROM `inheritor` WHERE `display_name` = '开发测试·吴匠人'
  );

-- ---------------------------------------------------------------------------
-- 5. Courses. OFFLINE is the only service_mode currently frozen by V002.
-- Store teaching and home-visit teaching are distinguished by title/location
-- until the application freezes separate service_mode codes.
-- ---------------------------------------------------------------------------

INSERT INTO `course`
  (`title`, `cover`, `summary`, `description`, `inheritor_id`, `heritage_project_id`,
   `category_id`, `service_mode`, `price`, `duration_minutes`, `city_code`, `location`,
   `audit_status`, `publish_status`, `is_recommend`, `recommend_sort`, `published_at`)
SELECT
  '开发测试·单人锡绣体验课', '/static/img/lbt1.jpg',
  '虚构开发测试课程：一人一席体验基础针法。',
  '仅用于开发环境，课程、讲师和场地均不代表真实经营信息。',
  i.`id`, hp.`id`, cc.`id`, 'OFFLINE', 88.00, 90, '320200',
  '开发测试·无锡非遗体验馆一号教室（虚构地址）', 1, 1, 1, 1, NOW()
FROM `inheritor` i
JOIN `heritage_project` hp ON hp.`id` = (
  SELECT MIN(hp1.`id`) FROM `heritage_project` hp1 WHERE hp1.`name` = '蜀绣'
)
JOIN `category` cc ON cc.`biz_type` = 'COURSE' AND cc.`code` = 'COURSE_INDIVIDUAL'
WHERE i.`display_name` = '开发测试·林师傅'
  AND NOT EXISTS (SELECT 1 FROM `course` WHERE `title` = '开发测试·单人锡绣体验课');

INSERT INTO `course`
  (`title`, `cover`, `summary`, `description`, `inheritor_id`, `heritage_project_id`,
   `category_id`, `service_mode`, `price`, `duration_minutes`, `city_code`, `location`,
   `audit_status`, `publish_status`, `is_recommend`, `recommend_sort`, `published_at`)
SELECT
  '开发测试·亲子泥塑手工课', '/static/img/lbt2.jpg',
  '虚构开发测试课程：亲子共同完成泥塑作品。',
  '仅用于开发环境，课程、讲师和场地均不代表真实经营信息。',
  i.`id`, hp.`id`, cc.`id`, 'OFFLINE', 128.00, 120, '320200',
  '开发测试·无锡非遗体验馆亲子教室（虚构地址）', 1, 1, 1, 2, NOW()
FROM `inheritor` i
JOIN `heritage_project` hp ON hp.`id` = (
  SELECT MIN(hp1.`id`) FROM `heritage_project` hp1 WHERE hp1.`name` = '银花丝'
)
JOIN `category` cc ON cc.`biz_type` = 'COURSE' AND cc.`code` = 'COURSE_FAMILY'
WHERE i.`display_name` = '开发测试·周老师'
  AND NOT EXISTS (SELECT 1 FROM `course` WHERE `title` = '开发测试·亲子泥塑手工课');

INSERT INTO `course`
  (`title`, `cover`, `summary`, `description`, `inheritor_id`, `heritage_project_id`,
   `category_id`, `service_mode`, `price`, `duration_minutes`, `city_code`, `location`,
   `audit_status`, `publish_status`, `is_recommend`, `recommend_sort`, `published_at`)
SELECT
  '开发测试·多人团建竹编课', '/static/img/lbt3.jpg',
  '虚构开发测试课程：多人协作完成竹编主题作品。',
  '仅用于开发环境，课程、讲师和场地均不代表真实经营信息。',
  i.`id`, hp.`id`, cc.`id`, 'OFFLINE', 168.00, 180, '320200',
  '开发测试·无锡团队活动中心（虚构地址）', 1, 1, 1, 3, NOW()
FROM `inheritor` i
JOIN `heritage_project` hp ON hp.`id` = (
  SELECT MIN(hp1.`id`) FROM `heritage_project` hp1 WHERE hp1.`name` = '竹编技艺'
)
JOIN `category` cc ON cc.`biz_type` = 'COURSE' AND cc.`code` = 'COURSE_TEAM'
WHERE i.`display_name` = '开发测试·林师傅'
  AND NOT EXISTS (SELECT 1 FROM `course` WHERE `title` = '开发测试·多人团建竹编课');

INSERT INTO `course`
  (`title`, `cover`, `summary`, `description`, `inheritor_id`, `heritage_project_id`,
   `category_id`, `service_mode`, `price`, `duration_minutes`, `city_code`, `location`,
   `audit_status`, `publish_status`, `is_recommend`, `recommend_sort`, `published_at`)
SELECT
  '开发测试·亲子剪纸上门体验课', '/static/img/logo1.jpg',
  '虚构开发测试课程：用于演示预约后确认上门教学地址。',
  '当前数据库仅确认 OFFLINE 服务编码；上门场景通过标题和地点说明表达。',
  i.`id`, hp.`id`, cc.`id`, 'OFFLINE', 198.00, 120, '320200',
  '开发测试·上门教学地址由测试预约填写（虚构）', 1, 1, 1, 4, NOW()
FROM `inheritor` i
JOIN `heritage_project` hp ON hp.`id` = (
  SELECT MIN(hp1.`id`) FROM `heritage_project` hp1 WHERE hp1.`name` = '川剧变脸'
)
JOIN `category` cc ON cc.`biz_type` = 'COURSE' AND cc.`code` = 'COURSE_FAMILY'
WHERE i.`display_name` = '开发测试·周老师'
  AND NOT EXISTS (SELECT 1 FROM `course` WHERE `title` = '开发测试·亲子剪纸上门体验课');

INSERT INTO `course`
  (`title`, `cover`, `summary`, `description`, `inheritor_id`, `heritage_project_id`,
   `category_id`, `service_mode`, `price`, `duration_minutes`, `city_code`, `location`,
   `audit_status`, `publish_status`, `is_recommend`, `recommend_sort`, `published_at`)
SELECT
  '开发测试·待审核竹编体验课', '/static/img/logo1.jpg',
  '虚构开发测试课程，用于覆盖待审核和未发布状态。',
  '该记录不应进入首页。', i.`id`, hp.`id`, cc.`id`, 'OFFLINE', 66.00, 60,
  '320200', '开发测试·待审核场地（虚构地址）', 0, 0, 0, 90, NULL
FROM `inheritor` i
JOIN `heritage_project` hp ON hp.`id` = (
  SELECT MIN(hp1.`id`) FROM `heritage_project` hp1 WHERE hp1.`name` = '竹编技艺'
)
JOIN `category` cc ON cc.`biz_type` = 'COURSE' AND cc.`code` = 'COURSE_INDIVIDUAL'
WHERE i.`display_name` = '开发测试·陈教习'
  AND NOT EXISTS (SELECT 1 FROM `course` WHERE `title` = '开发测试·待审核竹编体验课');

INSERT INTO `course`
  (`title`, `cover`, `summary`, `description`, `inheritor_id`, `heritage_project_id`,
   `category_id`, `service_mode`, `price`, `duration_minutes`, `city_code`, `location`,
   `audit_status`, `publish_status`, `is_recommend`, `recommend_sort`, `published_at`)
SELECT
  '开发测试·审核未通过剪纸课', '/static/img/logo1.jpg',
  '虚构开发测试课程，用于覆盖审核拒绝和未发布状态。',
  '该记录不应进入首页。', i.`id`, hp.`id`, cc.`id`, 'OFFLINE', 58.00, 60,
  '320200', '开发测试·审核未通过场地（虚构地址）', 2, 0, 0, 91, NULL
FROM `inheritor` i
JOIN `heritage_project` hp ON hp.`id` = (
  SELECT MIN(hp1.`id`) FROM `heritage_project` hp1 WHERE hp1.`name` = '银花丝'
)
JOIN `category` cc ON cc.`biz_type` = 'COURSE' AND cc.`code` = 'COURSE_INDIVIDUAL'
WHERE i.`display_name` = '开发测试·吴匠人'
  AND NOT EXISTS (SELECT 1 FROM `course` WHERE `title` = '开发测试·审核未通过剪纸课');

-- ---------------------------------------------------------------------------
-- 6. Course sessions. The home query reads capacity - booked_count as remaining.
-- ---------------------------------------------------------------------------

SET @phase1_session_anchor = DATE_ADD(NOW(), INTERVAL 2 HOUR);

INSERT INTO `course_session`
  (`course_id`, `start_time`, `end_time`, `city_code`, `location`, `capacity`, `booked_count`, `status`)
SELECT c.`id`, @phase1_session_anchor, DATE_ADD(@phase1_session_anchor, INTERVAL 90 MINUTE),
       '320200', '开发测试·一号教室场次', 1, 0, 1
FROM `course` c
WHERE c.`title` = '开发测试·单人锡绣体验课'
  AND NOT EXISTS (
    SELECT 1 FROM `course_session` cs
    WHERE cs.`course_id` = c.`id` AND cs.`location` = '开发测试·一号教室场次'
  );

INSERT INTO `course_session`
  (`course_id`, `start_time`, `end_time`, `city_code`, `location`, `capacity`, `booked_count`, `status`)
SELECT c.`id`, DATE_ADD(@phase1_session_anchor, INTERVAL 30 MINUTE),
       DATE_ADD(@phase1_session_anchor, INTERVAL 150 MINUTE),
       '320200', '开发测试·亲子教室场次', 12, 4, 1
FROM `course` c
WHERE c.`title` = '开发测试·亲子泥塑手工课'
  AND NOT EXISTS (
    SELECT 1 FROM `course_session` cs
    WHERE cs.`course_id` = c.`id` AND cs.`location` = '开发测试·亲子教室场次'
  );

INSERT INTO `course_session`
  (`course_id`, `start_time`, `end_time`, `city_code`, `location`, `capacity`, `booked_count`, `status`)
SELECT c.`id`, DATE_ADD(@phase1_session_anchor, INTERVAL 60 MINUTE),
       DATE_ADD(@phase1_session_anchor, INTERVAL 240 MINUTE),
       '320200', '开发测试·团建中心场次', 30, 18, 1
FROM `course` c
WHERE c.`title` = '开发测试·多人团建竹编课'
  AND NOT EXISTS (
    SELECT 1 FROM `course_session` cs
    WHERE cs.`course_id` = c.`id` AND cs.`location` = '开发测试·团建中心场次'
  );

INSERT INTO `course_session`
  (`course_id`, `start_time`, `end_time`, `city_code`, `location`, `capacity`, `booked_count`, `status`)
SELECT c.`id`, DATE_ADD(@phase1_session_anchor, INTERVAL 90 MINUTE),
       DATE_ADD(@phase1_session_anchor, INTERVAL 210 MINUTE),
       '320200', '开发测试·上门教学场次', 8, 7, 1
FROM `course` c
WHERE c.`title` = '开发测试·亲子剪纸上门体验课'
  AND NOT EXISTS (
    SELECT 1 FROM `course_session` cs
    WHERE cs.`course_id` = c.`id` AND cs.`location` = '开发测试·上门教学场次'
  );

INSERT INTO `course_session`
  (`course_id`, `start_time`, `end_time`, `city_code`, `location`, `capacity`, `booked_count`, `status`)
SELECT c.`id`, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 1 HOUR),
       '320200', '开发测试·停用场次', 10, 0, 0
FROM `course` c
WHERE c.`title` = '开发测试·待审核竹编体验课'
  AND NOT EXISTS (
    SELECT 1 FROM `course_session` cs
    WHERE cs.`course_id` = c.`id` AND cs.`location` = '开发测试·停用场次'
  );

INSERT INTO `course_session`
  (`course_id`, `start_time`, `end_time`, `city_code`, `location`, `capacity`, `booked_count`, `status`)
SELECT c.`id`, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 1 HOUR),
       '320200', '开发测试·已结束场次', 6, 6, 2
FROM `course` c
WHERE c.`title` = '开发测试·审核未通过剪纸课'
  AND NOT EXISTS (
    SELECT 1 FROM `course_session` cs
    WHERE cs.`course_id` = c.`id` AND cs.`location` = '开发测试·已结束场次'
  );

UPDATE `course_session` cs
JOIN `course` c ON c.`id` = cs.`course_id`
SET cs.`start_time` = @phase1_session_anchor,
    cs.`end_time` = DATE_ADD(@phase1_session_anchor, INTERVAL 90 MINUTE)
WHERE c.`title` = '开发测试·单人锡绣体验课'
  AND cs.`location` = '开发测试·一号教室场次';

UPDATE `course_session` cs
JOIN `course` c ON c.`id` = cs.`course_id`
SET cs.`start_time` = DATE_ADD(@phase1_session_anchor, INTERVAL 30 MINUTE),
    cs.`end_time` = DATE_ADD(@phase1_session_anchor, INTERVAL 150 MINUTE)
WHERE c.`title` = '开发测试·亲子泥塑手工课'
  AND cs.`location` = '开发测试·亲子教室场次';

UPDATE `course_session` cs
JOIN `course` c ON c.`id` = cs.`course_id`
SET cs.`start_time` = DATE_ADD(@phase1_session_anchor, INTERVAL 60 MINUTE),
    cs.`end_time` = DATE_ADD(@phase1_session_anchor, INTERVAL 240 MINUTE)
WHERE c.`title` = '开发测试·多人团建竹编课'
  AND cs.`location` = '开发测试·团建中心场次';

UPDATE `course_session` cs
JOIN `course` c ON c.`id` = cs.`course_id`
SET cs.`start_time` = DATE_ADD(@phase1_session_anchor, INTERVAL 90 MINUTE),
    cs.`end_time` = DATE_ADD(@phase1_session_anchor, INTERVAL 210 MINUTE)
WHERE c.`title` = '开发测试·亲子剪纸上门体验课'
  AND cs.`location` = '开发测试·上门教学场次';

UPDATE `course_session` cs
JOIN `course` c ON c.`id` = cs.`course_id`
SET cs.`start_time` = DATE_ADD(NOW(), INTERVAL 1 DAY),
    cs.`end_time` = DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 1 HOUR)
WHERE c.`title` = '开发测试·待审核竹编体验课'
  AND cs.`location` = '开发测试·停用场次';

UPDATE `course_session` cs
JOIN `course` c ON c.`id` = cs.`course_id`
SET cs.`start_time` = DATE_SUB(NOW(), INTERVAL 3 DAY),
    cs.`end_time` = DATE_ADD(DATE_SUB(NOW(), INTERVAL 3 DAY), INTERVAL 1 HOUR)
WHERE c.`title` = '开发测试·审核未通过剪纸课'
  AND cs.`location` = '开发测试·已结束场次';

-- ---------------------------------------------------------------------------
-- 7. Wuxi activities with visible, pending and ended state coverage
-- ---------------------------------------------------------------------------

INSERT INTO `activity`
  (`name`, `cover`, `description`, `location`, `start_time`, `end_time`,
   `organizer_id`, `organizer_name`, `limit_count`, `signup_count`, `status`, `type`,
   `city_code`, `address_detail`, `is_recommend`, `recommend_sort`)
SELECT
  '开发测试·周末锡绣开放体验', '/static/img/lbt1.jpg',
  '虚构开发测试活动，用于首页同城活动展示。',
  '开发测试·无锡非遗体验馆（虚构地址）', DATE_ADD(NOW(), INTERVAL 1 DAY),
  DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 2 HOUR), i.`id`,
  '开发测试·林师傅', 20, 6, 1, 0, '320200',
  '开发测试·无锡非遗体验馆（虚构地址）', 1, 1
FROM `inheritor` i
WHERE i.`display_name` = '开发测试·林师傅'
  AND NOT EXISTS (SELECT 1 FROM `activity` WHERE `name` = '开发测试·周末锡绣开放体验');

INSERT INTO `activity`
  (`name`, `cover`, `description`, `location`, `start_time`, `end_time`,
   `organizer_id`, `organizer_name`, `limit_count`, `signup_count`, `status`, `type`,
   `city_code`, `address_detail`, `is_recommend`, `recommend_sort`)
SELECT
  '开发测试·亲子泥塑体验日', '/static/img/lbt2.jpg',
  '虚构开发测试活动，用于首页同城活动展示。',
  '开发测试·无锡亲子手作空间（虚构地址）', DATE_ADD(NOW(), INTERVAL 2 DAY),
  DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 3 HOUR), i.`id`,
  '开发测试·周老师', 16, 8, 1, 0, '320200',
  '开发测试·无锡亲子手作空间（虚构地址）', 1, 2
FROM `inheritor` i
WHERE i.`display_name` = '开发测试·周老师'
  AND NOT EXISTS (SELECT 1 FROM `activity` WHERE `name` = '开发测试·亲子泥塑体验日');

INSERT INTO `activity`
  (`name`, `cover`, `description`, `location`, `start_time`, `end_time`,
   `organizer_id`, `organizer_name`, `limit_count`, `signup_count`, `status`, `type`,
   `city_code`, `address_detail`, `is_recommend`, `recommend_sort`)
SELECT
  '开发测试·社区竹编共创活动', '/static/img/lbt3.jpg',
  '虚构开发测试活动，用于首页同城活动展示。',
  '开发测试·无锡社区文化空间（虚构地址）', DATE_ADD(NOW(), INTERVAL 3 DAY),
  DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 3 HOUR), i.`id`,
  '开发测试·林师傅', 30, 18, 1, 0, '320200',
  '开发测试·无锡社区文化空间（虚构地址）', 1, 3
FROM `inheritor` i
WHERE i.`display_name` = '开发测试·林师傅'
  AND NOT EXISTS (SELECT 1 FROM `activity` WHERE `name` = '开发测试·社区竹编共创活动');

INSERT INTO `activity`
  (`name`, `cover`, `description`, `location`, `start_time`, `end_time`,
   `organizer_id`, `organizer_name`, `limit_count`, `signup_count`, `status`, `type`,
   `city_code`, `address_detail`, `is_recommend`, `recommend_sort`)
SELECT
  '开发测试·非遗家庭手作市集', '/static/img/logo1.jpg',
  '虚构开发测试活动，用于首页同城活动展示。',
  '开发测试·无锡文化广场（虚构地址）', DATE_ADD(NOW(), INTERVAL 4 DAY),
  DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 4 HOUR), i.`id`,
  '开发测试·周老师', 50, 21, 1, 0, '320200',
  '开发测试·无锡文化广场（虚构地址）', 1, 4
FROM `inheritor` i
WHERE i.`display_name` = '开发测试·周老师'
  AND NOT EXISTS (SELECT 1 FROM `activity` WHERE `name` = '开发测试·非遗家庭手作市集');

INSERT INTO `activity`
  (`name`, `cover`, `description`, `location`, `start_time`, `end_time`,
   `organizer_name`, `limit_count`, `signup_count`, `status`, `type`,
   `city_code`, `address_detail`, `is_recommend`, `recommend_sort`)
SELECT
  '开发测试·待审核非遗分享会', '/static/img/logo1.jpg',
  '虚构开发测试活动，用于覆盖待审核状态。',
  '开发测试·待审核场地（虚构地址）', DATE_ADD(NOW(), INTERVAL 5 DAY),
  DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 2 HOUR),
  '开发测试组织者', 20, 0, 0, 0, '320200',
  '开发测试·待审核场地（虚构地址）', 0, 90
WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `name` = '开发测试·待审核非遗分享会');

INSERT INTO `activity`
  (`name`, `cover`, `description`, `location`, `start_time`, `end_time`,
   `organizer_name`, `limit_count`, `signup_count`, `status`, `type`,
   `city_code`, `address_detail`, `is_recommend`, `recommend_sort`)
SELECT
  '开发测试·已结束手作回顾场', '/static/img/logo1.jpg',
  '虚构开发测试活动，用于覆盖已结束状态。',
  '开发测试·历史活动场地（虚构地址）', DATE_SUB(NOW(), INTERVAL 4 DAY),
  DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 2 HOUR),
  '开发测试组织者', 12, 12, 2, 0, '320200',
  '开发测试·历史活动场地（虚构地址）', 0, 91
WHERE NOT EXISTS (SELECT 1 FROM `activity` WHERE `name` = '开发测试·已结束手作回顾场');

UPDATE `activity`
SET `start_time` = DATE_ADD(NOW(), INTERVAL 1 DAY),
    `end_time` = DATE_ADD(DATE_ADD(NOW(), INTERVAL 1 DAY), INTERVAL 2 HOUR)
WHERE `name` = '开发测试·周末锡绣开放体验';

UPDATE `activity`
SET `start_time` = DATE_ADD(NOW(), INTERVAL 2 DAY),
    `end_time` = DATE_ADD(DATE_ADD(NOW(), INTERVAL 2 DAY), INTERVAL 3 HOUR)
WHERE `name` = '开发测试·亲子泥塑体验日';

UPDATE `activity`
SET `start_time` = DATE_ADD(NOW(), INTERVAL 3 DAY),
    `end_time` = DATE_ADD(DATE_ADD(NOW(), INTERVAL 3 DAY), INTERVAL 3 HOUR)
WHERE `name` = '开发测试·社区竹编共创活动';

UPDATE `activity`
SET `start_time` = DATE_ADD(NOW(), INTERVAL 4 DAY),
    `end_time` = DATE_ADD(DATE_ADD(NOW(), INTERVAL 4 DAY), INTERVAL 4 HOUR)
WHERE `name` = '开发测试·非遗家庭手作市集';

UPDATE `activity`
SET `start_time` = DATE_ADD(NOW(), INTERVAL 5 DAY),
    `end_time` = DATE_ADD(DATE_ADD(NOW(), INTERVAL 5 DAY), INTERVAL 2 HOUR)
WHERE `name` = '开发测试·待审核非遗分享会';

UPDATE `activity`
SET `start_time` = DATE_SUB(NOW(), INTERVAL 4 DAY),
    `end_time` = DATE_ADD(DATE_SUB(NOW(), INTERVAL 4 DAY), INTERVAL 2 HOUR)
WHERE `name` = '开发测试·已结束手作回顾场';

COMMIT;
