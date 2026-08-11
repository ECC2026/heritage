# 第一阶段需求差距分析

> 文档状态：技术设计基线，不代表已经实现。  
> 分析范围：微信 C 端首页、非遗传承人专区、非遗商城、手作体验与活动预约、非遗科普知识库、C 端个人中心、种草分享。  
> 明确排除：AI 虚拟体验、AI 数字人、AI 语音问答、AI 知识答题、AI 动漫人物管理、AI 音色管理。

## 1. 审计口径与证据

本结论同时核对了需求、C 端页面、后端实体与 Controller、管理后台路由/API，不以 README 或页面文案单独作为实现依据。

- 一期范围：`docs/requirements/phase-1-scope.md:13-38`。
- 原始业务需求：`docs/requirements/source-requirements.docx` 中“微信 C 端小程序详细功能需求”“总运营管理后台功能需求”。
- C 端有效路由：`client_code/pages.json:2-107`，现有 21 个注册页面；TabBar 为首页、文创、活动、社区、我的，见 `client_code/pages.json:109-143`。
- C 端 API 汇总：`client_code/common/request/api.js:6-308`。
- 后端现有活动实体：`server_code/src/main/java/com/example/server_code/entity/` 下 18 个实体。
- 后端现有路由：`server_code/src/main/java/com/example/server_code/controller/` 下 12 个 Controller。
- 管理后台菜单：`manage_code/src/router/index.js:3-15`。
- 管理后台 API：`manage_code/src/api/modules/`。
- 正式项目当前不存在 `heritage-platform/SQL/` 目录，因此无法从正式版本库复核建表 SQL、约束和索引。数据库现状以下述 Java 实体及当前接口为活动模型；开始迁移前必须补一份脱敏 schema baseline。
- `server_code/src/main/resources/static/er.puml:1-140` 仍是宠物系统遗留 ER 图，不能作为本项目数据库依据。

## 2. 总体判断

当前系统已经具备“内容展示 + 单规格商品下单 + 活动报名 + 图文社区 + 个人记录”的演示闭环，但与一期目标之间仍有三类结构性差距：

1. **领域模型不足**：课程/场次/预约/核销、商品 SKU、售后、地址、关注、资质明细、优惠券、积分均没有独立模型。
2. **运营能力不足**：缺少非遗项目管理、知识词条管理、社区审核、课程排期、核销、售后、营销权益等后台模块。
3. **安全与一致性不足**：管理员增删改接口没有统一管理员鉴权；订单、库存、报名名额没有 Service 事务边界；传承人详情可能直接返回身份证、电话、证书原始路径。

因此不建议重写已有页面，而应先稳定数据模型和 API 契约，再按模块替换薄弱链路。

## 3. 七个模块现状

### 3.1 首页

**当前已有页面**

- `pages/index/index.vue`：轮播图、快捷入口、热门非遗项目、资讯、精选商品、近期活动；数据加载见 `client_code/pages/index/index.vue:134-188`。
- 搜索区域目前只是展示文案，没有搜索提交或结果页，见 `client_code/pages/index/index.vue:17-20`。

**当前数据表与接口**

- 表：`banner`、`heritage_project`、`news`、`product`、`activity`。
- 接口：`GET /api/banners/enable`、`GET /api/heritage-projects/all`、`GET /api/news`、`GET /api/products`、`GET /api/activities/enable`，封装位置为 `client_code/common/request/api.js:77-140`。

**当前管理后台**

- 已有轮播图、资讯、商品、活动管理，见 `manage_code/src/router/index.js:6-13`。
- 没有非遗项目管理、首页弹窗、推荐位编排、同城门店/体验馆管理。

**复用结论**

- 直接复用：轮播图、资讯/商品/活动卡片及对应公开查询。
- 改造复用：首页页面骨架和 `banner`；增加统一首页聚合 DTO、推荐位类型和生效时间。
- 必须新增：统一搜索、六大分类、非遗级别筛选、首页运营位、活动弹窗、城市/门店数据。

### 3.2 非遗传承人专区

**当前已有页面**

- 没有面向公众的传承人列表或详情路由。
- 仅有个人中心的认证申请页 `pages/profile/inheritor.vue`，支持姓名、电话、身份证、技艺介绍、经历和单张证明上传，见 `client_code/pages/profile/inheritor.vue:21-53`。

**当前数据表与接口**

- `inheritor` 包含 `userId/name/phone/idCard/skillType/skillDesc/experience/certificate/auditStatus`，见 `server_code/src/main/java/com/example/server_code/entity/Inheritor.java:13-28`。
- 已有列表、详情、我的申请、提交申请、审核接口，见 `InheritorController#getInheritors`、`getInheritorById`、`getMyApplication`、`apply`、`auditInheritor`（`server_code/src/main/java/com/example/server_code/controller/InheritorController.java:41-170`）。

**当前管理后台**

- 已有传承人列表、详情和通过/拒绝操作，见 `manage_code/src/views/inheritants/index.vue:149-216`。
- 没有多资质归档、脱敏预览、审核日志、项目关联、作品/服务管理。

**复用结论**

- 直接复用：登录用户申请、基础审核状态展示。
- 改造复用：`inheritor` 作为公开档案主表；公开接口必须只返回已审核且启用的数据，并使用脱敏 DTO。
- 必须新增：资格证件子表、传承脉络、从业年限、服务标签、作品相册/视频、非遗项目关联、关注、咨询入口、课程/商品聚合。

### 3.3 非遗商城

**当前已有页面**

- `pages/shop/list.vue`：名称搜索和商品列表。
- `pages/shop/detail.vue`：价格、库存、销量、收藏、加入购物车、立即购买。
- `pages/shop/cart.vue`：查询、前端数量调整、选择结算；页面明确说明后端仅支持加入和查询，见 `client_code/pages/shop/cart.vue:6-8`。
- `pages/shop/order.vue`：临时填写收件人、电话、地址并提交订单。
- `pages/profile/orders.vue`：订单列表及确认收货。

**当前数据表与接口**

- 表：`category`、`product`、`cart`、`order`、`order_item`、`favorite`。
- 接口：商品列表/详情/CRUD/状态/库存，购物车增加/查询，订单创建/列表/状态/导出。
- `EntityController#createOrder` 直接把订单设为已支付并立即逐项扣库存，见 `server_code/src/main/java/com/example/server_code/controller/EntityController.java:563-630`。

**当前管理后台**

- 商品 CRUD/上下架和订单列表、发货、导出，见 `manage_code/src/views/products/index.vue:184-329`、`manage_code/src/views/orders/index.vue:140-223`。

**复用结论**

- 直接复用：商品列表/详情基础 UI、购物车入口、订单列表展示、商品和订单后台页面骨架。
- 改造复用：商品主表、订单主表/明细表、购物车；补 SKU、库存版本、地址快照、履约方式、状态机。
- 必须新增：商品规格、SKU、库存流水、多地址、快递/自提、运费模板、售后单、食品资质关联、订单状态日志。真实微信支付不在本次实现范围，设计上只预留支付单和回调幂等边界。

### 3.4 手作体验与活动预约

**当前已有页面**

- `pages/activity/list.vue`、`pages/activity/detail.vue`：活动展示、收藏、备注报名和我的报名状态。
- `pages/profile/signups.vue`：报名记录和取消报名。
- `pages/reservation/*` 是未注册且已停用的模板页，见 `client_code/pages/reservation/list.vue:1-28`。

**当前数据表与接口**

- `activity`：单一开始/结束时间、地点、人数上限和报名计数。
- `signup`：活动、用户、审核状态和取消时间。
- `SignupController#createSignup` 有可报名、人数上限和重复报名检查，但“查询名额—写报名—刷新计数”没有事务/原子条件，见 `server_code/src/main/java/com/example/server_code/controller/SignupController.java:147-197`。

**当前管理后台**

- 活动 CRUD/审核/结束；报名列表、审核和导出。

**复用结论**

- 直接复用：活动资讯型列表/详情、免费活动报名和报名记录。
- 改造复用：`activity/signup` 保留为公共活动模型，统一状态枚举并增加报名事务。
- 必须新增：课程、课程场次、服务模式、价格、容量、预约单、参与人、电子核销码、核销记录、改期/退款规则、研学套餐。

### 3.5 非遗科普知识库

**当前已有页面**

- `pages/news/list.vue`、`pages/news/detail.vue`：资讯列表、详情和收藏。
- 首页展示 `heritage_project` 卡片，但没有注册的非遗项目列表/详情页面；首页项目卡片也没有详情跳转方法。
- `pages/studyroom/*` 是已停用模板，不能视为知识库实现。

**当前数据表与接口**

- `heritage_project` 只有名称、封面、分类字符串、描述、地区、级别和状态，见 `HeritageProject.java:13-23`。
- `news` 可以承载政策、展会、通知类资讯。
- 已有项目与资讯的查询/CRUD接口，但管理后台只有资讯管理，没有非遗项目管理菜单。

**复用结论**

- 直接复用：资讯列表/详情、资讯收藏、资讯后台。
- 改造复用：`heritage_project` 作为权威非遗名录主体，`news` 保持时效资讯定位。
- 必须新增：知识词条、章节/富媒体、工艺步骤、项目分类/等级字典、项目与传承人关系、短视频资源、分享素材。

### 3.6 C 端个人中心

**当前已有页面**

- 登录、注册、资料编辑、订单、报名、帖子、收藏、传承人申请均已注册，见 `client_code/pages.json:7-107`。
- 登录状态由 `common/session.js` 维护，认证请求由 `common/request/request.js` 注入 Token。

**当前数据表与接口**

- `user` 支持用户名、密码、手机号、头像、昵称、性别、生日。
- 现有登录是手机号/用户名加密码，不是微信授权或短信验证码，见 `UserController#login`（`server_code/src/main/java/com/example/server_code/controller/UserController.java:29-66`）。
- 注册仍使用 MD5，且兼容明文密码，见 `UserController#register`、`matchesPassword`（`server_code/src/main/java/com/example/server_code/controller/UserController.java:73-100,166-170`）。

**当前管理后台**

- 用户列表、详情和启用/禁用；没有地址、权益、售后和登录身份绑定管理。

**复用结论**

- 直接复用：个人中心布局、资料编辑、订单/报名/帖子/收藏入口。
- 改造复用：用户账户和 JWT 会话；密码需升级为自适应哈希并提供平滑迁移。
- 必须新增：微信身份绑定、短信验证码登录、多地址、售后、关注、优惠券、积分账户/流水、统一订单与预约摘要。

### 3.7 种草分享社交板块

**当前已有页面**

- `pages/community/index.vue`：分类流、点赞、评论。
- `pages/community/post.vue`：标题、分类、正文和多图上传。
- `pages/community/detail.vue`、`pages/profile/posts.vue`：详情、评论、我的帖子和删除。

**当前数据表与接口**

- `post`、`comment`、`post_like` 已有实体和 Mapper；`favorite` 可收藏帖子。
- 现有 API 支持列表、详情、发布、删除、点赞、评论，见 `CommunityController`（`server_code/src/main/java/com/example/server_code/controller/CommunityController.java:49-268`）。

**当前管理后台**

- 没有帖子/评论审核、精选置顶、违规下架或达人资料管理。

**复用结论**

- 直接复用：图文发布、列表、详情、点赞、评论、我的帖子。
- 改造复用：将 `post` 产品化为“笔记”；图片字符串改为媒体子表，补审核工作流和标签。
- 必须新增：短视频、标准标签、挂载商品/课程/传承人、精选、分享素材包、达人标识、内容审核后台。
- 小红书联动一期应定义为“生成适配文案/海报/视频素材并记录分享行为”；跨平台直接发布或双向跳转必须在主体入驻和开放平台审核后单独验收，不能作为纯前端必然能力。

## 4. 功能差距矩阵

优先级：P0 为领域与安全前置；P1 为一期核心闭环；P2 为一期增强项。

| 需求功能 | 当前实现 | 缺失内容 | 前端改造 | 后端改造 | 数据库改造 | 管理后台改造 | 优先级 |
|---|---|---|---|---|---|---|---|
| 首页内容聚合 | 页面并发调用5类接口 | 统一排序、降级和运营编排 | 保留卡片，接入聚合 DTO | 新增 `/api/home` | 首页推荐位/弹窗配置 | 首页运营位配置 | P1 |
| 全局搜索 | 仅静态搜索文案 | 跨项目、传承人、课程、商品检索 | 新增搜索页和结果分栏 | 新增统一搜索服务 | 搜索字段/索引 | 热词与搜索词管理可后置 | P1 |
| 非遗分类/级别 | 项目表存字符串 | 固定分类、级别字典、筛选 | 发现页筛选 | 字典查询接口 | 规范化字典和外键 | 分类/等级维护 | P0 |
| 权威非遗名录 | 首页少量卡片 | 列表、详情、官方编号和来源 | 新增列表/详情 | 改造项目公开 DTO | 扩展项目表 | 新增项目管理 | P1 |
| 传承人公开专区 | 无 C 端页面 | 列表、详情、筛选、脱敏 | 新增列表/详情 | 改造公开查询 | 扩展主表和关系表 | 档案运营管理 | P1 |
| 传承人资质审核 | 单记录、单证书 | 多证件、有效期、审核日志、私有访问 | 改造申请页 | 资质上传/审核/脱敏接口 | 资质表、审核日志 | 多资料审核台 | P0 |
| 作品与服务 | 无 | 相册、视频、服务标签 | 详情页新增区块 | 作品/服务 API | 作品、服务关系表 | 作品/服务管理 | P1 |
| 关注传承人 | 无 | 关注/取消、粉丝数 | 关注按钮、关注列表 | 关注 API | `user_follow` | 风险账号处理 | P1 |
| 商品分类 | 商品有 categoryId | 分类接口/后台入口不完整 | 分类筛选 | 分类公开 API | 分类用途约束 | 分类管理 | P1 |
| 商品规格/SKU | 单商品单库存 | 规格组合、SKU价格库存 | 规格选择器 | SKU/库存服务 | SKU、规格、库存流水 | SKU编辑和库存台账 | P0 |
| 购物车 | 仅添加、查询 | 改数量、删除、失效处理 | 完整购物车交互 | PUT/DELETE API | 购物车关联 SKU | 无需独立后台 | P1 |
| 收货地址 | 下单时临时输入 | 多地址、默认地址 | 地址管理/选择 | 地址 CRUD | `user_address` | 客服只读脱敏查看 | P1 |
| 商城订单 | 创建后直接已支付 | 待支付、取消、履约、状态机、幂等 | 订单确认/详情/状态 | 订单 Service 与状态机 | 扩展订单/日志 | 状态操作权限化 | P0 |
| 库存扣减 | 读后写、无事务 | 原子预占/扣减/释放 | 只展示可售库存 | 事务、乐观锁、幂等 | SKU库存版本/流水 | 库存调整留痕 | P0 |
| 售后 | 无 | 申请、证据、审核、退款状态 | 售后申请/详情 | 售后工作流 API | 售后单/日志 | 售后仲裁 | P1 |
| 食品资质 | 无商品关联 | 强制校验和公开证照 | 商品详情证照入口 | 上架校验 | 商家资质及商品关系 | 资质审核/到期提醒 | P0 |
| 普通活动报名 | 可用的报名/审核 | 并发名额、规则、事务 | 保留现有流程 | 重构报名事务 | 唯一键/名额一致性 | 保留报名审核 | P0 |
| 手作课程 | 无独立模型 | 课程类型、服务模式、价格 | 课程列表/详情 | 课程 API | `course` | 课程审核管理 | P1 |
| 课程场次 | 无 | 日期时段、容量、地点、讲师 | 场次选择 | 场次/余位 API | `course_session` | 排期管理 | P1 |
| 预约/改期/退款 | 活动 signup 不能覆盖 | 预约订单和规则 | 预约确认/详情 | 预约状态机 | `reservation`、变更日志 | 预约处理 | P1 |
| 电子核销 | 无 | 核销码、核销权限、幂等 | C端展示核销码 | 核销生成/验证 API | 核销凭证/记录 | 核销工作台 | P1 |
| 科普词条 | 项目描述+资讯 | 历史、工艺、文化章节 | 知识库列表/详情 | 词条 API | `knowledge_entry`/媒体 | 词条编辑审核 | P1 |
| 微信/短信登录 | 密码登录 | OpenID绑定、短信验证码 | 登录页增加方式 | 授权码换取身份/短信校验 | 微信身份、验证码记录 | 账号绑定查询 | P0 |
| 收藏 | 支持资讯/商品/活动/帖子 | 项目、传承人、课程 | 扩展收藏分类 | 扩展目标校验 | 保持通用表并扩类型 | 无需独立后台 | P1 |
| 优惠券 | 无 | 领券、使用、核销、回退 | 券包/下单选择 | 优惠计算服务 | 券模板/用户券/日志 | 营销配置 | P2 |
| 积分 | 无 | 账户、获取、抵扣、流水 | 积分首页/流水 | 积分规则与幂等账本 | 积分账户/流水/规则 | 规则配置与调整审核 | P2 |
| 种草笔记 | 图文帖子可用 | 视频、标签、挂载业务对象 | 改造发布/详情 | 笔记媒体和挂载 API | 媒体、标签、挂载关系 | 内容审核/精选 | P1 |
| 小红书联动 | 无 | 分享素材、分享记录、平台资质 | 生成海报/复制文案 | 分享素材 API | 分享记录 | 素材导出 | P2 |
| 统一管理员权限 | 前端仅检查本地 token | 后端 RBAC、接口鉴权、审计 | 无 | 管理员鉴权拦截/权限注解 | 角色、权限、审计日志 | 角色权限配置 | P0 |

## 5. 复用分类

### 5.1 可以直接复用

- 首页、商品、活动、资讯、社区和个人中心现有视觉骨架。
- `Result` 统一返回结构和 MyBatis-Plus 分页能力。
- Banner、资讯、基础商品、活动、报名、帖子、评论、点赞、收藏的普通查询。
- 管理后台 Banner、资讯、商品、活动、报名、用户、传承人页面骨架。

### 5.2 可以改造后复用

- `heritage_project`、`inheritor`、`product`、`order`、`activity`、`signup`、`post`、`favorite`。
- `pages/community/*` 改造成发现页下的种草内容，而非继续占据独立一级入口。
- `news` 保留“时效资讯”，不要继续兼任结构化科普词条。
- 现有 Controller 路由可保留兼容，但业务写操作应下沉 Service。

### 5.3 必须新增

- 非遗分类/级别字典、项目与传承人关系、传承人资质、作品、服务标签、关注。
- 商品规格/SKU、库存流水、地址、订单状态日志、售后。
- 课程、场次、预约、改期记录、核销凭证/记录。
- 知识词条与媒体、笔记媒体/标签/挂载关系。
- 微信身份、短信验证码、优惠券、积分账户和流水。
- 管理员 RBAC、审核日志、敏感文件私有访问。

## 6. 安全与工程前置差距

1. 后端不存在 Service 目录；Controller 直接操作多个 Mapper。订单和报名无法获得可靠事务边界。
2. `EntityController`、`ActivityController`、`BannerController`、`HeritageProjectController` 等管理写接口没有管理员身份校验；管理后台前端路由守卫不能代替服务端授权。
3. 管理员和用户密码使用 MD5；用户登录还兼容明文，见 `UserController.java:166-170` 和 `AdminController.java:55-58`。
4. `GET /api/inheritors/{id}` 直接返回实体，可能包含 `phone/idCard/certificate`，见 `InheritorController.java:75-81`。
5. 上传接口只校验扩展名，且 `/uploads/**` 全部公开，见 `UploadController.java:29-52`、`WebMvcConfig.java:17-22`；资质文件不能继续走该公开资源链路。
6. 创建订单直接设为已支付并扣库存，且无事务/乐观锁，见 `EntityController.java:614-627`。
7. 报名名额通过先查询后写入实现，在并发下可能超卖，见 `SignupController.java:167-196`。
8. 删除/状态接口缺少合法状态迁移校验和幂等键；订单、退款、核销必须先建立状态机。

## 7. 推荐开发顺序

1. **P0-1 基线冻结**：补正式 schema baseline、统一枚举/术语、冻结页面与 API 命名。
2. **P0-2 安全骨架**：C 端/管理员身份分离、RBAC、私有文件、密码迁移、审计日志。
3. **P0-3 核心字典与档案**：非遗分类/级别、非遗项目、传承人资质和公开 DTO。
4. **P0-4 交易基础**：SKU、库存事务、地址、订单状态机，再接商城页面。
5. **P1-1 课程预约**：课程—场次—预约—核销完整模型，再开发对应页面。
6. **P1-2 发现与知识库**：项目、传承人、知识词条的列表/详情/搜索。
7. **P1-3 社交升级**：笔记媒体、标签、挂载、审核与精选。
8. **P1-4 个人中心汇总**：订单、预约、收藏、关注、地址、售后入口。
9. **P2 营销权益**：优惠券、积分、分享素材和运营数据。

每一步应按“迁移设计确认 → 数据库迁移 → 后端接口 → 管理后台 → C 端页面 → 联调验收”顺序推进，避免页面先行导致字段和接口反复修改。
