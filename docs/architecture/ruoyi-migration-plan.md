# ICHIP 后端迁移至 RuoYi-Vue-Pro 架构方案

> 文档状态：架构迁移分析基线，不代表迁移已经执行。  
> 核对日期：2026-08-12。  
> 当前系统：`heritage-platform/server_code`。  
> 目标上游：YunaiV/ruoyi-vue-pro `master-jdk17`。  
> 本轮边界：只分析、设计和规划；未修改业务代码、数据库、API 或小程序页面，未拉取或运行 RuoYi。

## 1. 推荐架构与架构决策

### 1.1 推荐结论

采用 **RuoYi-Vue-Pro 单体多模块作为新后端基础平台，使用绞杀式迁移逐域替换 legacy-server**：

```text
微信小程序（继续使用现有 uni-app Vue3）
        │
        ├── /api/**           → 迁移期 legacy-server 或兼容路由
        └── /app-api/**       → 新 RuoYi 后端
                                  ├── system / infra
                                  ├── member / pay / mall
                                  └── heritage

RuoYi Vue3 Admin             → /admin-api/**
                                  ↓
                              MySQL + Redis
```

核心决策如下：

1. **不在 `server_code` 中复制 RuoYi Security、日志或支付类。** 新建独立 `backend/`，以固定的 `master-jdk17` 提交为基线，`server_code` 在迁移期间只作为旧服务运行。
2. **先保持单体，不上 Spring Cloud。** 当前业务规模和三人团队不需要微服务复杂度；RuoYi 单体多模块已经能提供清晰边界。
3. **通用能力归 RuoYi，非遗领域归 Heritage。** 认证、RBAC、日志、文件、支付、会员、商城不在 Heritage 重造；非遗项目、传承人、课程、预约、核销、知识和 B 端合作不塞进 System/Mall。
4. **旧库只读备份后做可回放 ETL。** 不删除旧表，不直接把旧表改造成 RuoYi 表；使用新库或新 schema、迁移批次和 ID 映射表。
5. **API 先兼容，后收敛。** 小程序页面不批量重写；先由兼容适配层保持旧字段、旧状态和 `code=200`，然后逐个 service 函数迁移到 `/app-api/**`。
6. **首页作为第一个贯通切片。** 先迁移城市、Banner、非遗门类/级别、项目及首页聚合读接口，证明新基线、数据迁移、接口兼容、日志和监控均可工作。
7. **支付最后接真实渠道。** Phase 5 先使用 RuoYi 模拟支付验证订单、支付单、回调和退款状态机，真实微信商户配置需单独审批。
8. **`legacy-server` 只有在流量、数据和回滚窗口全部关闭后才删除。** Phase 10 前禁止删除旧服务和旧表。

### 1.2 上游版本基线

本地工作区未提供 RuoYi 源码。本方案核对了官方仓库和官方文档：

- 官方 `master-jdk17` 根 `pom.xml` 当前声明 Java 17、Spring Boot 3.5.15、项目版本 `2026.06-SNAPSHOT`；当前旧后端为 Java 17、Spring Boot 3.5.13。
- 上游 `yudao-dependencies/pom.xml` 当前声明 MyBatis-Plus 3.5.16、MyBatis 3.5.19、Redisson 4.6.1、Springdoc 2.8.17；当前旧后端显式使用 MyBatis-Plus 3.5.5。
- 官方分支在 2026-06-30 有发布提交 `ec3f7cb`。正式实施时必须记录**完整 commit SHA**，不得直接跟随浮动分支构建。
- 官方根 POM 默认只启用 System、Infra；Member、Pay、Mall 是注释状态。目标基线必须显式启用这三个模块并导入各自 SQL，不能因为源码目录存在就认定功能已启用。

上游依据：

- [RuoYi-Vue-Pro 官方仓库](https://github.com/YunaiV/ruoyi-vue-pro)
- [`master-jdk17` 根 POM](https://raw.githubusercontent.com/YunaiV/ruoyi-vue-pro/master-jdk17/pom.xml)
- [`master-jdk17` 依赖 BOM](https://raw.githubusercontent.com/YunaiV/ruoyi-vue-pro/master-jdk17/yudao-dependencies/pom.xml)
- [官方项目结构说明](https://doc.iocoder.cn/project-intro/)

说明：上游是持续变化的外部依赖。“RuoYi 已有”表示上游提供实现基础，不等于启用后自动满足本项目生产要求；仍需完成配置、裁剪、权限建模、密钥管理和专项测试。

## 2. 当前后端审计与能力对比

### 2.1 当前实现的关键证据

| 结论 | 当前代码证据 |
|---|---|
| Java/Spring Boot | `server_code/pom.xml:7-12,39-41`：Spring Boot 3.5.13、Java 17 |
| MyBatis | `server_code/pom.xml:49-53`：MyBatis-Plus 3.5.5；20 个 Mapper |
| Redis | `server_code/pom.xml:79-83` 和 `application.properties:20-24` 只有依赖/连接配置；Java 源码未发现 RedisTemplate、Redisson 或缓存使用 |
| JWT | `JwtUtil.java:28-42` 本地签发 JWT；多个 Controller 各自解析 Authorization，例如 `UserController.java:151-163` |
| 密码 | `UserController.java:166-174` 同时接受明文或 MD5；`AdminController.java:55-58` 使用 MD5 |
| 管理权限 | `EntityController.java:50-119,133-227,391-496,517-689` 等管理 CRUD 没有统一管理员认证/RBAC 注解 |
| Controller 过重 | `EntityController.java` 812 行，直接处理用户、资讯、演出、商品、订单和统计；订单创建直接操作多个 Mapper |
| 事务 | Java 源码未发现 `@Transactional`；订单、报名、评论等存在多表写入 |
| 参数校验 | POM 有 validation starter，但业务 Controller 未发现 `@Valid/@Validated`；大量使用 `Map<String,Object>` |
| 返回结构 | `common/Result.java:10-33` 统一了 `code/message/data`，但业务错误多以 HTTP 200 + code 表达，错误码没有领域规范 |
| 异常处理 | 未发现 `@RestControllerAdvice/@ExceptionHandler` |
| 文件 | `UploadController.java:23-52` 仅按扩展名校验并写本地磁盘；`WebMvcConfig.java:18-21` 将 `/uploads/**` 全部公开 |
| 日志/监控/API 文档 | 未发现操作日志、访问日志、异常日志、Actuator/监控、Springdoc/Knife4j 配置 |
| 测试 | 仅 `ServerCodeApplicationTests.java` 上下文测试；无业务、权限、并发、接口或迁移测试 |
| 数据库演进 | 仓库只有 `V002__phase1_home_foundation.sql` 与开发 seed；没有发现迁移运行器配置 |

### 2.2 能力比较矩阵

| 能力 | 当前已有 | RuoYi 已有 | 需要迁移/接入 | 需要保留 | 需要废弃 |
|---|---|---|---|---|---|
| Spring Boot | 3.5.13 | `master-jdk17` 当前 3.5.15 | 新基线统一采用上游 BOM，禁止在 Heritage 单独锁冲突版本 | Java 17 | 旧 POM 三段重复 compiler 配置 |
| MyBatis/MyBatis-Plus | MP 3.5.5，BaseMapper + XML | MP 3.5.16、MyBatis starter、分页/审计基类 | Entity/Mapper 按 RuoYi DO/Mapper 规范重建；迁移 SQL 独立维护 | 已验证的复杂查询语义 | Controller 直接拼 QueryWrapper 的数据访问方式 |
| Redis | 有 starter 和配置，无业务使用 | Spring Data Redis + Redisson、缓存、MQ | Token、幂等、锁、限流、缓存统一用框架组件 | 现有 Redis 部署参数概念 | “引了依赖即具备能力”的假设 |
| 认证 | 自签 JWT，Controller 手动解析 | Spring Security + Token + Redis，多用户类型 | Admin 走 System，C 端走 Member；迁移期双 Token 只在边界适配 | 现有小程序 token 存储和 Authorization 头 | `JwtUtil`、各 Controller 的 `getCurrentUser` |
| 权限 | 基本没有统一权限 | RBAC、菜单/按钮权限、数据权限、`@PreAuthorize` | 建立 heritage 权限码、角色、部门/数据范围 | 当前运营岗位业务知识 | 公开路径上的后台 CRUD |
| 登录/操作/API/异常日志 | 仅保存最后登录 IP/时间，MyBatis stdout | 登录日志、操作日志、API 访问日志、API 异常日志、traceId | 启用 Infra/System 日志并配置脱敏、留存周期和告警 | 必要业务审计字段 | 生产 SQL stdout、记录敏感请求体 |
| 文件 | 本地目录 + 全公开 URL | Infra File，支持本地、DB、S3/MinIO和私有桶预签名 | 公开媒体与私有资质分桶/配置；文件元数据和引用迁移 | 旧 URL 兼容读取期 | 资质证件放 `/uploads/**`、仅扩展名校验 |
| 支付/退款 | 无真实支付中心；订单仅有 payTime | Pay 应用、渠道、支付单、退款单、通知与回调 | Mall 订单接 Pay API；回调验签、幂等、对账；先模拟后微信 | 当前未支付订单作历史记录 | 在 Heritage 自写微信支付 SDK/回调 |
| 商品/购物车/订单 | 单规格 Product、Cart、Order/Item；简单状态 | Mall 的 Product SPU/SKU、Trade、售后、营销、库存 | 迁移到 Mall；金额元转分；状态和库存策略显式映射 | 商品文化关联、旧订单快照、业务展示文案 | 旧 `EntityController` 订单实现和客户端传 userId |
| 会员 | `user`，密码/手机号/个人资料 | Member 用户、地址、等级、积分、社交登录 | `user → member_user`；微信身份绑定；保留 legacy ID 映射 | 昵称、头像、手机号、历史关系 | 明文/MD5兼容登录长期存在 |
| 异常处理/返回 | 简单 `Result` | `CommonResult`、全局异常、错误码 | 新 API 统一用 RuoYi；兼容层翻译旧 `code=200` | 旧客户端契约只在过渡期保留 | Controller 捕获/吞掉异常和通用 500 文案 |
| 参数校验 | 有依赖，使用很少 | Jakarta Validation + VO 分层 | ReqVO/RespVO、枚举/字典校验、文件 MIME 校验 | 现有字段业务规则 | `Map<String,Object>` 作为写接口契约 |
| 事务 | 未发现事务注解 | Spring 事务及成熟业务服务范式 | 订单、库存、名额、核销、退款必须服务层事务 + 唯一约束 | 旧状态语义用于迁移映射 | Controller 多 Mapper 非事务写入 |
| 幂等 | 无 | Protection starter 的 `@Idempotent` | 创建订单、报名、核销、退款、合作表单和回调使用业务幂等键 | 已有唯一业务号（核对后使用） | toggle 型写接口作为关键动作 |
| 分布式锁 | 无 | Redisson/Lock4j | 仅用于跨实例临界区；与数据库条件更新/唯一键共同使用 | 无 | 以 JVM `synchronized` 代替分布式一致性 |
| 限流 | 无 | Protection starter RateLimiter | 登录、验证码、搜索、上传、表单、支付回调分场景限流 | 无 | Heritage 自建第二套 AOP 限流 |
| API 文档 | 无 | Springdoc + Knife4j | 所有新接口写 OpenAPI 注解；CI 校验契约 | 现有 `phase1-api-design.md` 作为需求输入 | 只靠 README/代码猜接口 |
| 监控 | 无 | Spring Boot Admin、Actuator，支持 SkyWalking | 健康、JVM、线程池、DB、Redis、HTTP、业务指标与告警 | 现有运行端口/健康需求 | 无监控直接切流 |
| 测试 | 1 个上下文测试 | Test starter、JUnit/Mockito，大量上游测试 | Heritage 单测、Mapper 集成、API 契约、迁移对账、并发和安全测试 | 可复用的接口测试样例 | 只以“能编译/能启动”为验收 |

## 3. 目标后端结构与模块边界

### 3.1 推荐目录

```text
backend/
├─ pom.xml
├─ yudao-dependencies/                 # 上游 BOM；尽量不改
├─ yudao-framework/                    # 上游通用 starter；禁止放非遗业务
├─ yudao-module-system/                # 管理员、组织、RBAC、字典、日志
├─ yudao-module-infra/                 # 文件、配置、API 日志、任务、监控
├─ yudao-module-member/                # C 端会员、地址、积分、微信身份
├─ yudao-module-pay/                   # 支付、退款、渠道、回调通知
├─ yudao-module-mall/                  # SPU/SKU、购物车、交易、售后、营销
├─ yudao-module-heritage/
│  ├─ yudao-module-heritage-api/       # 跨模块 DTO/API，不暴露 Mapper/DO
│  └─ yudao-module-heritage-biz/
│     └─ src/main/java/.../heritage/
│        ├─ controller/admin/           # /admin-api/heritage/**
│        ├─ controller/app/             # /app-api/heritage/**
│        ├─ controller/app/compat/      # 临时旧 API 适配，带删除期限
│        ├─ dal/dataobject/
│        ├─ dal/mysql/
│        ├─ service/
│        ├─ convert/
│        ├─ enums/
│        └─ job/
└─ yudao-server/                       # 只负责装配启用的模块

legacy-server/                         # 迁移期指现有 server_code，不移动也不覆盖
```

`backend/` 的具体落盘需在 Phase 1 获得实施授权后进行。本轮不创建该目录。

### 3.2 Heritage 内部领域包

| 领域包 | 职责 | 不应承担 |
|---|---|---|
| `project` | 非遗项目、非遗门类、级别、地区、名录档案 | 商品分类、系统字典底座 |
| `productsystem` | 六大产品体系、跨域内容编排和映射 | SPU/SKU、库存、交易 |
| `inheritor` | 传承人公开档案、资质、作品、项目关系、服务能力 | 登录账户、私有文件存储实现 |
| `knowledge` | 知识词条、章节、来源、项目/传承人关系 | 新闻资讯冒充结构化知识 |
| `course` | 课程、场次、授课主体、服务模式 | 通用活动报名 |
| `reservation` | 课程预约、名额、改期/取消、核销凭证和记录 | 支付渠道实现 |
| `activity` | 文化活动、报名、活动核销、演艺关联 | Mall 商品订单 |
| `content` | Banner、非遗资讯、首页领域聚合配置 | System 公告、通用文件 |
| `community` | 种草笔记、评论、点赞、内容审核 | 即时聊天和客服 |
| `cooperation` | B 端合作线索、附件引用、分派和跟进日志 | CRM 全功能复制 |

### 3.3 是否进一步拆模块

首期**不拆更多 Maven 顶层模块**。上述领域先作为 `yudao-module-heritage-biz` 内的包和数据库前缀边界，原因是：

- 三人团队若拆 8～10 个 Maven 模块，会增加 API 模块、依赖管理和联调成本；
- 项目、传承人、知识、课程之间关系密集，当前没有独立部署或独立发布需求；
- 单体内可通过 package、Service 接口和 ArchUnit 规则约束依赖，不必先微服务化。

仅在满足以下任一条件后再评估拆分：社区内容有独立审核/流量团队；预约同时服务多个非遗外业务；B 端演化为完整 CRM；某领域需要独立部署扩缩容。Mall、Pay、Member 已由 RuoYi 独立模块提供，不并入 Heritage。

### 3.4 模块依赖规则

```text
heritage-biz ──→ heritage-api
heritage-biz ──→ member-api / infra-api / mall-api / pay-api（仅通过公开 API）
mall-biz     ──→ member-api / pay-api
yudao-server ──→ 各 biz 模块（装配）
```

- Heritage 不直接查询 `member_user`、`product_spu`、`pay_order`，通过对应 API 或只保存稳定 ID。
- Mall 不反向依赖 Heritage；商品与非遗项目/传承人的关系放 Heritage 关系表。
- 不允许模块间注入对方 Mapper，不允许数据库跨模块级联删除。
- 是否启用多租户必须在 Phase 1 决定。若当前只有一个平台，建议首期关闭业务多租户，但保留上游通用字段与兼容性，不自行删除 tenant 基础设施。

## 4. 现有数据迁移映射

### 4.1 总体策略

1. 新建独立目标数据库，例如 `heritage_yudao`；旧 `heritage` 数据库保持可回滚只读副本。
2. 先导入固定 SHA 对应的 RuoYi System/Infra/Member/Pay/Mall 基线 SQL，再应用 Heritage 版本化 migration。
3. 建立 `heritage_migration_batch` 和 `heritage_legacy_id_map(entity_type, legacy_id, target_id, batch_id, checksum)`；不依赖“目标 ID 恰好等于旧 ID”。
4. ETL 使用可重复运行的 staging/import 脚本：抽取 → 清洗 → 校验 → 导入 → 对账。每批记录数量、金额合计、孤儿数和校验和。
5. 金额从旧 `DECIMAL(10,2)` 元转换为 RuoYi整数分时，使用 `ROUND(value * 100)` 并做精度、负数和溢出校验。
6. 迁移期禁止双端任意写同一业务表。每个领域明确“旧写新读/双读/新写”的切换点；必要的双写必须走 outbox/重试和对账，不能在 Controller 中裸双写。
7. 迁移脚本只新增目标记录，不删除旧记录；失败回滚以撤销迁移批次或丢弃目标 schema 为主。

### 4.2 表映射矩阵

| 旧表 | 新表/模块 | 迁移策略 | 保留与风险控制 |
|---|---|---|---|
| `user` | `member_user` / Member | 映射 mobile、nickname、avatar、gender、birthday、status、login 信息；建立 user ID 映射 | 不搬明文/MD5作为可登录密码；导入为不可密码登录，走微信绑定/验证码/重置 |
| `admin` | `system_users` / System | 创建系统用户、部门、岗位、角色关系；逐账号人工核对 | MD5 不迁移；强制重置密码和 MFA/强认证策略（若启用）；旧 admin 切流后禁用 |
| `category` (`biz_type=HERITAGE`) | `heritage_category` / Heritage | 保留 code/name/层级/排序/状态；项目关系改用映射后 ID | 不与六大产品体系合并 |
| `category` (`biz_type=PRODUCT`) | `product_category` / Mall | 建立商品分类映射，校验层级和启用状态 | 其他 biz_type 分流，不能整表直接导入 Mall |
| `heritage_level` | `heritage_level` / Heritage | 按稳定 code 导入；保持 NATIONAL/PROVINCIAL/MUNICIPAL/DISTRICT | code 作为对账键，不靠中文名 |
| `heritage_project` | `heritage_project` / Heritage | 迁移名录字段、分类/级别/城市和推荐元数据；保留旧文本快照用于核对 | `official_code` 冲突和空值单独报告，不伪造认证数据 |
| `inheritor` | `heritage_inheritor`、`heritage_inheritor_qualification` / Heritage | 公开档案与私有申请/资质拆开；关联 `member_user` | phone、idCard、certificate 不进入公开 DTO；私有文件迁入私有存储 |
| `product` | `product_spu` + `product_sku` / Mall | 每个旧商品生成 1 个 SPU + 默认 SKU；图片、类目、价格、库存、销量映射 | 元转分；旧状态映射需显式；文化关系另存 Heritage |
| 商品—项目/传承人关系（当前缺失或弱关联） | `heritage_product_relation` / Heritage | 可确认的关系导入；无法确认的不猜测 | 关联指向 Mall SPU ID，支持项目/传承人/作品类型 |
| `cart` | `trade_cart` / Mall | user→member、product→SKU 后导入有效购物项；失效商品单列 | 切换窗口前可选择不迁购物车并明确告知用户，但不能静默丢失 |
| `order` | `trade_order` / Mall | 订单头、收货快照、金额、状态、时间迁入；保留 legacy_order_no | 历史订单默认只读；不要把 `pay_time` 当作真实 Pay 成功证据 |
| `order_item` | `trade_order_item` / Mall | 映射 order/SPU/SKU；保留商品名、封面、价格快照 | 对账订单总额与明细合计；异常订单隔离而非自动修正 |
| 新支付数据 | `pay_app/pay_channel/pay_order/pay_refund/pay_notify_*` / Pay | 仅新系统切换后创建；历史未接真实支付的不伪造 pay_order | 旧订单可标记 `legacyPayment=true`，无渠道流水不生成假记录 |
| `activity` | `heritage_activity` / Heritage | 迁移时间、地点、城市、人数、类型和状态；显式状态映射 | 先修复 `signup_count` 与报名明细差异 |
| `signup` | `heritage_activity_signup` / Heritage | user/activity 映射后迁移；保留审核/取消时间和报名快照 | 唯一约束防同用户重复有效报名；手机号脱敏 |
| `course` | `heritage_course` / Heritage | 迁移课程、项目、传承人、服务方式及审核/发布状态 | 当前只支撑首页展示，不能误判为已有完整预约能力 |
| `course_session` | `heritage_course_session` / Heritage | 迁移时间、容量、booked_count、城市和地点 | 对账预约后再决定 booked_count，不能只信缓存计数 |
| 预约/核销（当前无正式表） | `heritage_reservation`、`heritage_verification_record` / Heritage | 新建，不从 signup 强行转换；活动 signup 与课程 reservation 分开 | token 哈希存储、唯一键、操作人和审计时间 |
| `performance` | `heritage_performance` / Heritage | 迁移公开演艺资料；可映射至产品体系“民俗演艺” | 不塞入 Mall SPU，除非未来售票模型确认 |
| `news` | `heritage_news` / Heritage | 迁移资讯、来源、推荐/置顶和发布时间 | 不等同结构化知识词条 |
| `banner` | `heritage_banner` / Heritage | 迁移图片、链接、排序、状态；清洗 linkType/目标 | 外链白名单，失效目标不展示 |
| `post` | `heritage_note` / Heritage Community | 迁移作者、正文、图片、分类和审核状态 | 旧 status=1 不能自动等同已完成生产内容审核，需策略确认 |
| `comment` | `heritage_note_comment` | 映射帖子/用户/父评论，保留展示快照 | 检查孤儿父评论和删除语义 |
| `post_like` | `heritage_note_like` | 按 post/user 映射导入，唯一约束 | 重算点赞计数，不信旧冗余数 |
| `favorite` | Mall `product_favorite` 或 `heritage_user_favorite` | 按 type 分流：商品进 Mall，其余进 Heritage | 未知 type 隔离；不丢弃 |
| 本地 `uploads/**` | `infra_file` + 对象存储 / Infra | 计算 hash、检测 MIME、分类公开/私有后上传，生成 URL 映射 | 旧 URL 在兼容期只读；证件不可公开回源 |
| `city` | 优先复用 System 地区 + Heritage 城市运营配置 | 行政区划映射到 System area code；默认城市/排序保留在 Heritage 配置 | 不重复维护两套行政区划树 |

### 4.3 数据验收基线

- 每张表：源记录数、目标成功数、隔离数三者可对账。
- 每个外键：孤儿记录为 0，或全部进入有原因的 quarantine 表/报告。
- 用户、订单、报名：抽样逐条核对；订单总金额按状态分组核对到分。
- 密码、证件、手机号、资质文件：输出和日志不得出现明文。
- ETL 连续执行两次，不产生重复目标数据；迁移批次可以明确回滚。

## 5. API 兼容与映射策略

### 5.1 契约差异

当前小程序统一请求代码 `client_code/common/request/request.js:18-45` 只把 `payload.code === 200` 视为成功；RuoYi `CommonResult` 通常以成功码 `0` 返回。当前分页通常是 `{list,total,page,size}`，RuoYi 常用 `PageResult{list,total}`；商品还存在 `cover ↔ picUrl`、`price(元) ↔ price(分)`、状态枚举差异。因此不能只改域名。

迁移期采用两层兼容：

1. **边缘路由兼容**：同域暴露 `/api/**`（旧）和 `/app-api/**`（新），避免一次改完全部 service。
2. **Heritage Compat Facade**：只对尚未迁移的页面返回旧字段/旧 envelope；内部调用新 Service/API，不直接访问旧表。每个兼容接口登记 owner、调用页面、删除阶段和契约测试。

禁止长期维护两套业务实现。Compat 只做鉴权上下文转换、路径/字段/状态/金额/返回结构转换，不写复杂领域逻辑。

### 5.2 重点 API 映射

| 旧 API | 目标新 API | 兼容适配 | 前端影响 |
|---|---|---|---|
| `GET /api/home` | `GET /app-api/heritage/home` | **需要**：保持当前首页 DTO 和 `code=200`；内部新聚合 | 第一刀仅替换 `getHome` 的服务路由或由网关透明转发 |
| `GET /api/cities` | `GET /app-api/heritage/cities` | 需要，保持 code/name/default 字段 | 页面无需改 |
| `GET /api/heritage-categories` | `GET /app-api/heritage/categories` | 需要，保持六大门类语义 | 后续只改 `api.js` |
| `GET /api/heritage-levels` | `GET /app-api/heritage/levels` | 需要 | 无页面结构变化 |
| `GET /api/heritage-projects/all` | `GET /app-api/heritage/project/page` | 需要：旧全量/分页差异要设上限 | 列表 service 后续改分页 |
| `GET /api/heritage-projects/{id}` | `GET /app-api/heritage/project/get?id=` | 建议兼容旧 path | 详情页只换 service |
| `GET /api/search` | `GET /app-api/heritage/search` | 需要，聚合 Mall/Heritage 多域并统一摘要 | 搜索页面保持现有数据结构 |
| `GET /api/inheritors` | `GET /app-api/heritage/inheritor/page` | **必须**：仅返回公开 DTO，屏蔽 phone/idCard/certificate | 前端移除对敏感/旧字段的任何依赖 |
| `POST /api/inheritors/apply` | `POST /app-api/heritage/inheritor-application/create` | 需要；文件改为 Infra 私有 fileId | 认证表单 service 调整，页面布局可不改 |
| `POST /api/user/login` | `POST /app-api/member/auth/login` 或微信登录接口 | Phase 4 前保留旧登录；Token 不互认，由边界适配 | `session.js` 可保留；登录 API/过期处理一次调整 |
| `GET/PUT /api/user/info` | `GET/PUT /app-api/member/user/get/update` | 需要字段映射 | 个人中心 service 调整 |
| `GET /api/products` | RuoYi Mall App 商品分页 API | **强需要**：SPU字段、分单位、分页和状态转换 | 商品列表 service 换 DTO adapter |
| `GET /api/products/{id}` | RuoYi Mall App SPU 详情 API | 强需要：默认 SKU 与旧单规格视图 | 详情页保留 UI，逐步启用 SKU |
| `GET/POST /api/cart` | RuoYi Mall App Cart API | 需要 user/token 与 SKU 转换 | service 改为 skuId；页面后续支持规格 |
| `POST /api/orders` | RuoYi Mall Trade 订单创建/结算 API | **必须重构契约**，不能把旧请求原样透传 | 保留页面，service 增加结算确认步骤；服务端定价 |
| `GET /api/orders/my` | RuoYi Mall App Trade Order Page | 需要状态、金额、明细字段转换 | 订单页可渐进迁移 |
| `GET /api/activities/enable` | `GET /app-api/heritage/activity/page` | 需要保持现有卡片字段 | 列表 service 替换 |
| `GET /api/activities/{id}` | `GET /app-api/heritage/activity/get?id=` | 需要 | 详情 service 替换 |
| `POST /api/signups` | `POST /app-api/heritage/activity-signup/create` | 需要；当前用户取安全上下文并加幂等键 | 按钮加重复提交保护，不重写页面 |
| `GET /api/signups/my` | `GET /app-api/heritage/activity-signup/my-page` | 需要 | service 替换 |
| `GET/POST /api/posts` | `GET/POST /app-api/heritage/note/**` | 需要旧分类、媒体和审核状态映射 | 社区保留，晚于核心链路迁移 |
| `/api/favorites/**` | Mall 商品收藏 + Heritage 通用收藏 API | 需要按 type 路由，逐步废弃 toggle | service 内拆分，页面交互保持 |
| `POST /api/upload/image` | Infra File App 上传/预签名接口 | 需要按 usage 路由公开/私有配置 | `uploadImage` 增加 usage；资质不得获得永久公开 URL |
| 管理端当前 `/api/**` CRUD | `/admin-api/heritage/**`、`/admin-api/mall/**` | 不做永久兼容；管理后台按模块迁移 | RuoYi Admin 新页面逐模块替代旧 manage_code |

目标 Mall API 的最终 URL 必须以选定 SHA 的实际 Controller 和 OpenAPI 为准；上表描述迁移边界，不把未经本地拉取验证的上游路径当成冻结契约。

### 5.3 首页试点

首页试点只迁移读链路：

1. 导入 city/area 映射、Banner、非遗门类、级别、项目、公开传承人摘要及首页所需关联。
2. 新建 `HomeController(app) → HomeService → Heritage Mapper/Mall API`，返回当前首页 DTO。
3. 接入 RuoYi 访问日志、异常日志、traceId、Redis 缓存和失效策略。
4. 对同一固定数据集同时调用旧/新 `/home`，做 JSON schema、区块数量、ID、排序和空值对比。
5. 先灰度只读流量；错误率或数据差异超阈值时，路由立刻回旧 `/api/home`。

## 6. 生产安全能力迁移清单

| 能力 | 复用位置 | Heritage 应做 | 验收重点 |
|---|---|---|---|
| Spring Security | Framework Security + System/Member | 仅声明 App/Admin 接口权限与资源所有权 | 未登录 401、无权限 403；不得信任客户端 userId |
| Token + Redis | System/Member 认证体系 | 通过安全上下文取 userId/userType；定义会话期限和踢下线 | 退出立即失效、续期、并发登录策略、Redis 故障行为 |
| RBAC | System 用户/角色/菜单 | 权限码如 `heritage:project:query`、`heritage:qualification:audit` | 最小权限、按钮与 API 双校验 |
| 数据权限 | Biz Data Permission | 为运营城市、传承人本人、审核机构定义数据范围 | 同角色跨城市/跨主体不可越权 |
| 登录日志 | System | 配置管理员/会员登录成功失败日志和脱敏 | 失败原因可追踪但不记录密码/code |
| 操作日志 | System/Framework | 审核、上下架、库存调整、核销、退款、B端线索操作标注 | 操作人、对象、前后状态、traceId 完整 |
| API/异常日志 | Web starter + Infra | 敏感接口关闭 request/response body 或配置脱敏 | idCard、手机号、token、微信 code、支付报文不泄露 |
| 文件服务 | Infra File | 保存 fileId；公开内容和私有资质使用不同配置 | 真 MIME/大小校验、病毒扫描策略、短时预签名、访问审计 |
| 私有资质文件 | Infra 私有 S3/MinIO | 仅授权审核员/本人申请短时 URL | 数据库不存长期签名 URL；分享链接过期 |
| 分布式锁 | Protection/Redisson/Lock4j | 场次名额、核销、回调只在必要临界区加锁 | 锁超时、异常释放、多实例并发 |
| 幂等 | Protection `@Idempotent` + 业务唯一键 | 订单、报名、核销、退款、合作表单、回调定义 idempotency key | 同 key 同结果；不同 payload 冲突；重试不重复扣减 |
| 限流 | Protection RateLimiter | 登录、微信 code、搜索、上传、合作表单按用户/IP/接口组合限流 | 返回可识别错误码，不误伤正常回调 |
| 微信登录 | Member + System Social/微信小程序登录 | 配置 AppID/AppSecret 环境变量；绑定 legacy member | code 一次性、unionid/openid边界、账号合并审计 |
| 支付 | Pay | Mall 只调用 Pay API；应用标识与商户订单号唯一 | 金额服务端计算、签名、订单与支付单状态一致 |
| 退款 | Pay | 售后审批后创建退款单，业务状态等待回调确认 | 部分退款、重复退款、失败补偿、对账 |
| 支付回调 | Pay Notify + 业务回调 | 验签、原始报文留存脱敏、状态机条件更新 | 重放幂等、乱序、延迟、伪造回调、重复通知 |
| 监控 | Monitor/Actuator/Spring Boot Admin，可选 SkyWalking | 增加订单、支付、报名、核销、迁移差异业务指标 | 健康探针、告警阈值、日志 trace 关联 |
| 单元/集成测试 | Yudao test starter + JUnit/Mockito | 覆盖 Service、Mapper、权限、状态机、并发和 ETL | 关键领域有失败路径与边界测试，不只 happy path |
| API 文档 | Springdoc/Knife4j | ReqVO/RespVO 和错误码完整注解 | `/app-api` 与 `/admin-api` 分组、敏感示例脱敏 |

额外原则：框架能力必须按选定版本阅读源码和测试后使用；注解不是数据一致性的替代品。订单/名额/库存仍需事务、数据库唯一键、条件更新和对账任务共同保证。

## 7. 分阶段迁移计划

### Phase 0：当前系统冻结

| 项目 | 内容 |
|---|---|
| 输入 | 当前 Git 基线、数据库 schema/数据快照、API 文档、小程序和后台可运行版本 |
| 修改内容 | 冻结旧接口契约；建立接口清单、数据字典、状态枚举、敏感字段清单、基准流量和回归样例；记录上游完整 SHA |
| 影响文件 | 仅新增架构/契约/对账文档和测试资产；不改 `server_code` 业务 |
| 数据库影响 | 只读导出 schema、行数、校验和；不迁移、不 DDL |
| API 影响 | 无；为每个旧 API 标记调用页面、权限和 owner |
| 验收条件 | 可重复启动旧系统；关键 API golden response；完整备份恢复演练成功 |
| 回滚方案 | 无运行变更；丢弃分析资产即可 |

### Phase 1：RuoYi 独立基线

| 项目 | 内容 |
|---|---|
| 输入 | 固定 SHA、Java 17、MySQL/Redis 隔离实例、密钥管理方案 |
| 修改内容 | 新建 `backend/`；启用 System/Infra/Member/Pay/Mall；加入空 Heritage api/biz；配置环境变量、日志、OpenAPI、监控和 CI |
| 影响文件 | `backend/**`、部署/环境示例、CI；不覆盖 `server_code/**` |
| 数据库影响 | 新建目标库并导入固定版本基线 SQL；不接触旧表 |
| API 影响 | 只开放健康检查和 RuoYi 管理登录；业务 API 尚不切换 |
| 验收条件 | clean checkout 可编译/测试/启动；无默认密钥；RBAC、Redis token、日志、文件测试配置可用 |
| 回滚方案 | 停止新后端并丢弃隔离目标库；旧系统继续服务 |

### Phase 2：首页与基础非遗数据

| 项目 | 内容 |
|---|---|
| 输入 | 首页数据字典、现有 Home DTO、city/category/level/project/banner 数据 |
| 修改内容 | 建立 Heritage 项目/门类/级别/首页内容；实现首页 App API 和兼容 DTO；启用缓存、访问日志和契约测试 |
| 影响文件 | `backend/yudao-module-heritage/**`、ETL/对账脚本、网关路由、少量 `client_code/common/request/api.js`（切流时） |
| 数据库影响 | 新增 Heritage 基础表和映射/批次表；导入只读数据 |
| API 影响 | 新增 `/app-api/heritage/home` 等；旧 `/api/home` 保持可回退 |
| 验收条件 | 新旧首页区块、排序、ID和空状态对账；压测/错误率达标；小程序页面无需重写 |
| 回滚方案 | 路由切回旧 `/api/home`；保留目标数据供排错，不反写旧库 |

### Phase 3：传承人

| 项目 | 内容 |
|---|---|
| 输入 | 传承人公开/私有字段边界、认证流程、项目关系、文件清单 |
| 修改内容 | 公开档案、申请、资质、作品、审核、数据权限；私有文件迁 Infra；RuoYi Admin 管理页面首批迁移 |
| 影响文件 | Heritage inheritor 包、Infra 文件配置、Admin 传承人页面、C端 service |
| 数据库影响 | 新建传承人/资质/作品/关系/审核日志；迁移并脱敏 |
| API 影响 | 新列表/详情/申请/审核 API；旧公开接口由 compat 返回安全 DTO |
| 验收条件 | 公共响应无 phone/idCard/certificate；越权测试、文件过期和审核审计通过 |
| 回滚方案 | C端/管理路由切旧；新申请在切换窗口暂停或按批次回放，禁止双边同时审核 |

### Phase 4：会员与微信登录

| 项目 | 内容 |
|---|---|
| 输入 | `user` 数据、微信 AppID、合法 AppSecret 注入方式、账号合并规则 |
| 修改内容 | 迁 Member；接微信小程序登录/手机号或验证码方案；Token 切换；处理 legacy 用户绑定 |
| 影响文件 | Member 配置/扩展、身份映射 ETL、`client_code/common/session.js` 和登录 service（仅实施阶段） |
| 数据库影响 | `member_user`、social user/bind、token及 legacy ID map；旧密码不作为新密码 |
| API 影响 | 新 Member Auth；兼容期识别旧/新 token 的边界服务，不在业务 Controller 双解析 |
| 验收条件 | 新老用户、绑定、退出、过期、封禁、并发登录、日志和限流通过；历史关系正确 |
| 回滚方案 | 认证路由回旧；新建 Member 保留并停写；映射表保证再次迁移可续跑 |

### Phase 5：商城与支付

| 项目 | 内容 |
|---|---|
| 输入 | Product/Cart/Order 数据、状态映射、金额与库存规则、支付/售后需求 |
| 修改内容 | 启用 Mall/Pay；单规格商品转 SPU+默认 SKU；购物车、结算、订单、库存、支付、退款；文化关联留 Heritage |
| 影响文件 | Mall/Pay 配置与扩展、Heritage 商品关系、ETL、C端商城 service、RuoYi Mall Admin |
| 数据库影响 | product/trade/pay 系列表和映射；历史订单只读迁移；不删除旧表 |
| API 影响 | Mall App API 替代产品/购物车/订单；compat 转价格/状态/字段 |
| 验收条件 | 金额到分对账、库存并发、重复下单、模拟支付、重复/乱序回调、退款、历史订单查询通过 |
| 回滚方案 | 未支付新单停单并回旧商城；已进入新 Pay 的订单必须在新系统闭环，不跨系统退款；按订单创建时间划分责任系统 |

### Phase 6：预约与核销

| 项目 | 内容 |
|---|---|
| 输入 | Course/Session/Activity/Signup、容量规则、改期/取消/核销权限 |
| 修改内容 | Heritage 课程预约和活动报名分别建模；名额事务、锁、幂等、核销 token、工作人员数据权限 |
| 影响文件 | Heritage course/reservation/activity、管理页面、小程序活动/预约 service |
| 数据库影响 | reservation、verification、状态日志、唯一键；迁移 signup，校准计数 |
| API 影响 | 预约创建/取消/我的预约/核销 API；旧 signup 暂由 compat |
| 验收条件 | 高并发不超卖、重复核销同结果、无权核销失败、计数可重建、审计完整 |
| 回滚方案 | 新场次切回前冻结预约；已生成的新核销凭证留新系统处理，旧场次仍由旧系统处理 |

### Phase 7：B 端合作

| 项目 | 内容 |
|---|---|
| 输入 | 已确认表单字段、合作类型、隐私政策、线索分派和 SLA |
| 修改内容 | Cooperation 线索、附件、状态/跟进日志；公开提交限流幂等；后台分派、脱敏和审计 |
| 影响文件 | Heritage cooperation、Infra 私有文件、RuoYi Admin 页面、C端 B 端入口/表单 |
| 数据库影响 | 新表，无旧数据迁移；敏感字段加密/脱敏策略 |
| API 影响 | 提交、本人查询（如需要）、管理分页/分派/跟进 API |
| 验收条件 | 重复提交受控、附件私有、最小权限、导出审计、通知失败可重试 |
| 回滚方案 | 关闭 feature flag 和提交入口；已收线索保留在新库只读处理 |

### Phase 8：管理后台

| 项目 | 内容 |
|---|---|
| 输入 | 当前 11 个管理页面、角色矩阵、前述各域 Admin API |
| 修改内容 | 逐菜单迁到 RuoYi Vue3 Admin；动态菜单、按钮权限、数据权限、操作日志；旧 manage_code 只读过渡 |
| 影响文件 | 独立 RuoYi Admin 项目、System 菜单/权限 SQL；不一次复制旧页面 |
| 数据库影响 | system menu/role/permission 配置；不改业务主数据 |
| API 影响 | 管理接口全部收敛 `/admin-api/**`，不再与公开 API 共 Controller |
| 验收条件 | 各角色最小权限、敏感字段脱敏、导出水印/审计（按需求）、双后台结果对账 |
| 回滚方案 | 菜单或域级切回旧后台；已切域禁止两后台同时编辑 |

### Phase 9：安全与性能验收

| 项目 | 内容 |
|---|---|
| 输入 | 全量候选版本、生产等价环境、威胁模型、SLO和数据对账报告 |
| 修改内容 | SAST/依赖/密钥扫描、越权/注入/上传/回调测试、压测、故障注入、备份恢复、监控告警和 runbook |
| 影响文件 | 测试、CI、部署、告警和运维文档；只修复验收发现的问题 |
| 数据库影响 | 生产迁移演练与性能索引验证，不直接删旧库 |
| API 影响 | 冻结 v1 契约；清点 compat 调用量和删除条件 |
| 验收条件 | P0/P1 安全问题清零；SLO、恢复目标、对账、支付/库存/名额一致性和回滚演练通过 |
| 回滚方案 | 未达门槛不全量切流；按域或比例回旧服务 |

### Phase 10：删除 legacy-server

| 项目 | 内容 |
|---|---|
| 输入 | 至少一个约定观察周期内 compat/旧服务零业务流量、数据与财务对账签字、归档审批 |
| 修改内容 | 先下线路由和部署，再归档源码/镜像/配置；最后在单独审批中删除 legacy 代码 |
| 影响文件 | 部署清单、网关、运维文档；`server_code` 删除必须是独立任务/PR |
| 数据库影响 | 旧库先转只读归档；删除须满足法务/审计保留期并另行批准 |
| API 影响 | 移除旧 `/api/**` 与 compat；发布破坏性变更公告 |
| 验收条件 | 无旧调用、无未迁数据、归档可恢复、所有消费者已升级 |
| 回滚方案 | 观察期保留可启动镜像和只读库；下线后在窗口内可恢复路由，删除归档后不承诺即时回滚 |

## 8. 三人任务拆分

| 角色 | 主责 | 主要目录 | 不应越界 |
|---|---|---|---|
| A：平台/安全负责人 | 固定上游基线、System/Infra、Security、Redis、文件、日志、监控、CI/CD、Pay 基线、生产验收 | `backend/yudao-framework`（尽量少改）、System、Infra、Pay、`yudao-server`、部署 | 不直接实现 Heritage 业务规则或改小程序页面 |
| B：领域/数据负责人 | Heritage 模块、领域模型、Mapper/Service、数据迁移/对账、兼容 Facade、Mall 文化关联、预约核销 | `yudao-module-heritage/**`、migration/ETL、Heritage OpenAPI | 不复制框架安全能力，不直接改 System/Pay 内核 |
| C：客户端/后台/质量负责人 | 小程序 service 渐进切换、契约回归、RuoYi Admin 业务页面、E2E、测试数据与验收看板 | `client_code/common/request/**`（按阶段）、RuoYi Admin、接口/E2E测试 | 不绕过 service 在页面硬接新字段，不独立改数据库状态 |

协作规则：

- 每个 Phase 由 A 维护平台基线，B 冻结 API/数据契约，C 建立消费者契约测试；三人共同签署验收。
- 共享文件（根 POM、`yudao-server`、API enum、迁移总清单）指定单一 owner，避免并行冲突。
- 每个接口先提交 OpenAPI/DTO/错误码评审，再分别实现后端和前端；每个迁移先提交映射与对账规则，再写 ETL。
- 支付、退款、库存、名额、核销、隐私文件至少双人复核，不允许单人设计后直接切生产。

## 9. 风险清单

| 等级 | 风险 | 影响 | 控制措施 |
|---|---|---|---|
| 严重 | 旧管理 CRUD 基本无统一鉴权，迁移期若继续公网暴露可越权 | 数据泄露/篡改 | 迁移前先用网络边界限制旧后台；新后台全部 RBAC；安全测试 |
| 严重 | 明文/MD5密码兼容逻辑 | 账号接管 | 不迁旧 hash 为可登录密码；管理员强制重置；会员走微信/验证码/重置 |
| 严重 | 订单/库存/报名/核销跨表写无事务、幂等和并发保护 | 超卖、重复支付/报名 | 新域状态机、事务、唯一键、条件更新、幂等、锁和对账；按创建时间划责任系统 |
| 严重 | 支付回调跨新旧系统或重复处理 | 资金损失 | Pay 单一入口；验签；商户订单唯一；新订单不回旧退款；回调重放测试 |
| 高 | 用户/商品/订单 ID 改变导致关系错连 | 历史数据不可用 | 独立 ID map、批次校验、外键孤儿报告、抽样与总量对账 |
| 高 | 元转分、状态枚举、分页/返回码差异 | 金额/页面错误 | 显式映射表、契约测试、金额对账；compat 统一转换 |
| 高 | 私有资质沿用公开 `/uploads/**` | 身份与证件泄露 | 分类迁移到私有桶、短时预签名、权限和访问审计；旧 URL 下线 |
| 高 | `master-jdk17` 浮动升级引入不兼容 | 构建/运行漂移 | Fork 或锁完整 SHA、保留上游变更日志、依赖扫描、定期受控升级 |
| 高 | RuoYi 模块“存在但未启用”或示例配置带默认数据 | 功能缺失/弱配置 | 启用清单、环境隔离、删除示例凭证、配置扫描和基线测试 |
| 高 | 双写失败产生数据分叉 | 新旧数据不一致 | 尽量域级单写；必要时 outbox + 重试 + 对账，不做裸同步双写 |
| 中 | Heritage 直接依赖 Mall/Member Mapper 形成耦合 | 后续升级困难 | 只依赖 module-api，ArchUnit/代码评审约束 |
| 中 | 一次启用 RuoYi 全量模块扩大攻击面和维护面 | 构建慢、漏洞面增大 | 只启用 System/Infra/Member/Pay/Mall/Heritage；AI/BPM/CRM等默认关闭 |
| 中 | 上游通用 Mall 模型与非遗定制/服务不完全匹配 | 过度改 Mall 内核 | 标准实物交易用 Mall；文化关系/服务在 Heritage；通过 API 关联 |
| 中 | 管理后台同时编辑同一领域 | 覆盖/状态冲突 | 域级切换、旧后台只读、feature flag 和审计 |
| 中 | 缺少完整原始初始化 SQL，仓库 schema 与开发库可能漂移 | ETL 失败 | Phase 0 从实际库只读导出 schema 并与 Entity/V002 三方比对 |
| 低 | RuoYi 管理后台视觉和现有后台不一致 | 培训成本 | 先迁高风险管理域，提供角色菜单和操作手册，不追求像素复制 |

## 10. 回滚总策略

### 10.1 回滚单位

回滚以**业务域/路由**为单位，不以整个系统为唯一开关：Home、Heritage、Inheritor、Member、Mall、Reservation、Cooperation 分别配置 feature flag 和路由权重。

### 10.2 数据所有权

- 切换前：旧系统是唯一写主。
- 灰度读：新系统只读迁移副本，随时切回。
- 域切写后：新系统是该域唯一写主，旧后台转只读。
- 交易切换后：订单按 `system_origin + created_at` 固定责任系统；已经进入新 Pay 的订单不回旧系统退款。

### 10.3 回滚资产

- 固定版本的旧服务镜像、配置模板和数据库只读快照。
- 每次目标 migration 的前向脚本、迁移批次清理/隔离方案和对账报告。
- 网关旧/新路由配置、feature flag、健康和错误率阈值。
- ID 映射、旧 URL 映射、兼容契约测试和事件/outbox 重放能力。

### 10.4 禁止的伪回滚

- 不用 `DROP/DELETE` 目标表来“回滚”仍在处理的支付、订单或预约。
- 不在回滚时把新系统产生的数据直接覆盖回旧表。
- 不让两个系统同时无约束接受同一支付回调或核销 token。
- 不在没有恢复演练的情况下删除 `server_code` 或旧数据库。

## 11. 需要在实施前确认的决策

1. 锁定的 RuoYi 完整 commit SHA、fork 仓库和上游升级策略。
2. 单数据库多 schema、同实例双库还是独立实例；建议至少逻辑独立目标库。
3. 是否首期关闭多租户；若未来运营机构隔离明确，应在建表前决定 tenant 策略。
4. 旧会员登录迁移方式：仅微信/验证码重绑定，还是提供一次性密码升级窗口。
5. 历史订单是否只读展示；无真实支付流水的旧订单如何标识。
6. 商城首发是否立即支持多 SKU、售后、优惠券、积分；未启用能力不要伪装已迁移。
7. 活动报名和课程预约是否共用支付，以及退款/取消规则。
8. 私有文件存储选型、数据驻留、保留期限和访问审计要求。
9. Compat API 的最长存续期、调用量归零阈值与消费者升级责任人。
10. RuoYi MIT 许可虽宽松，仍需完成依赖许可证、CVE、示例账号/密钥和供应链扫描。

## 12. 本轮未执行事项

- 未创建 `backend/`，未下载或复制 RuoYi 源码。
- 未修改或覆盖 `server_code/**`。
- 未修改 `client_code/**` 或 `manage_code/**`。
- 未执行数据库 DDL、DML、导入、导出或迁移。
- 未修改任何现有 API、路由、业务逻辑或生产配置。
- 未接入微信登录、支付、退款、文件存储或 Redis 安全能力。
- 未删除旧表、旧服务或旧功能。
