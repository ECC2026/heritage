# 第一阶段 API 设计

> 文档状态：接口契约设计，不代表已实现。  
> API 根路径沿用现有 `/api`，避免为一期整体迁移额外引入版本前缀。  
> 明确排除：AI 虚拟体验、AI 数字人、AI 语音问答、AI 知识答题及相关素材管理接口。

## 1. 设计依据与对象命名

本文与以下设计保持一致：

- `docs/requirements/gap-analysis.md`：七个一期模块及复用边界；
- `docs/architecture/page-architecture.md`：首页、发现非遗、商城、活动、我的五个一级入口；
- `docs/database/phase1-database-design.md`：物理表与新增领域对象。

统一对象名称：

| 业务对象 | API 复数路径 | 主要数据表 |
|---|---|---|
| 非遗项目 | `heritage-projects` | `heritage_project`、`heritage_project_media` |
| 非遗传承人 | `inheritors` | `inheritor`、`inheritor_project`、`inheritor_qualification`、`inheritor_work` |
| 商品/SKU | `products` / `skus` | `product`、`product_sku`、规格表、库存流水 |
| 手作课程/场次 | `courses` / `course-sessions` | `course`、`course_session` |
| 预约 | `reservations` | `reservation`、`reservation_change_log` |
| 核销 | `verifications` | `verification_token`、`verification_record` |
| 知识词条 | `knowledge-entries` | `knowledge_entry`、`knowledge_section`、`knowledge_media` |
| 种草笔记 | `notes` | 兼容现有 `post`，以及 `post_media`、`post_tag`、`post_target` |
| 关注 | `follows` | `user_follow` |
| 地址 | `addresses` | `user_address` |
| 优惠券 | `coupons` | `coupon_template`、`user_coupon`、`coupon_use_log` |
| 积分 | `points` | `points_account`、`points_ledger`、`points_rule` |

物理表一期继续使用 `post`，对外产品术语和新增 API 统一使用“笔记/notes”。`signup` 只表示普通活动报名，不与课程预约 `reservation` 混用。

## 2. 通用契约

### 2.1 登录态与权限

1. C 端 Token 继续通过 `Authorization: Bearer <token>` 传递，与 `client_code/common/request/request.js:14-17` 一致。
2. 公共查询允许匿名；收藏、关注、购物车、订单、预约、发布笔记和个人中心必须登录。
3. 用户身份只能从服务端验证后的 Token 获取，禁止信任请求体中的 `userId`。
4. 管理员 Token 与 C 端 Token 必须区分主体类型或签发域；管理写接口统一迁移至 `/api/admin/**` 并校验权限。
5. 核销接口仅允许具备对应课程/活动权限的传承人或运营人员访问。
6. 公开传承人 DTO 不返回身份证、原始手机号、证书文件键等敏感字段。

### 2.2 统一分页

所有分页列表统一使用：

| 参数 | 类型 | 默认 | 规则 |
|---|---|---:|---|
| `page` | integer | 1 | 从1开始 |
| `size` | integer | 10 | 1～100，服务端限制上限 |
| `sort` | string | 模块默认值 | 只能使用接口声明的白名单，如 `publishedAt,desc` |

返回：

```json
{
  "list": [],
  "total": 0,
  "page": 1,
  "size": 10,
  "hasNext": false
}
```

现有接口已经返回 `list/total/page/size`，新增 `hasNext` 为兼容性增强，不要求旧页面立即使用。

### 2.3 统一返回结构

一期保留现有 `Result<T>` 的 `code/message/data`，避免一次性改造客户端。现状定义见 `server_code/src/main/java/com/example/server_code/common/Result.java:8-36`。

建议扩展为：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "errorCode": null,
  "traceId": "request-trace-id"
}
```

- `code` 与 HTTP 状态保持一致，不再所有错误都返回 HTTP 200。
- `errorCode` 是稳定的机器可读业务码，例如 `INVENTORY_NOT_ENOUGH`。
- `message` 面向用户，不作为客户端逻辑判断依据。
- `traceId` 用于服务端日志定位。

### 2.4 错误码原则

| HTTP/code | 场景 | 示例业务码 |
|---:|---|---|
| 400 | 参数格式错误 | `INVALID_ARGUMENT` |
| 401 | 未登录或 Token 失效 | `AUTH_REQUIRED`、`TOKEN_EXPIRED` |
| 403 | 已登录但无权操作 | `FORBIDDEN`、`VERIFICATION_FORBIDDEN` |
| 404 | 资源不存在或不可公开 | `RESOURCE_NOT_FOUND` |
| 409 | 状态/并发冲突 | `INVENTORY_NOT_ENOUGH`、`SESSION_FULL`、`STATE_CONFLICT` |
| 422 | 业务校验未通过 | `QUALIFICATION_REQUIRED`、`RESCHEDULE_NOT_ALLOWED` |
| 429 | 验证码或请求过频 | `RATE_LIMITED` |
| 500 | 未预期服务端错误 | `INTERNAL_ERROR` |

状态冲突必须返回当前状态和可执行动作，不能只返回“操作失败”。

### 2.5 幂等与并发

- 创建订单、创建预约、领取/使用优惠券、积分变更、退款和核销接受 `Idempotency-Key` 请求头。
- 同一用户、同一幂等键、同一业务操作返回同一结果。
- 库存、场次余位和核销使用数据库条件更新/唯一约束，不依赖客户端防重复点击。
- GET 不产生隐式写操作；浏览量等统计通过独立异步或显式事件处理。

## 3. 首页与统一搜索

### 3.1 首页方案

推荐首页采用一个聚合接口 `GET /api/home`，而不是让页面继续并发请求轮播图、项目、资讯、商品、活动五个接口。

理由：

1. 首页区块顺序、数量、推荐规则属于运营配置，应由服务端一次返回。
2. 聚合层可对单个区块超时做降级，不让某一个接口失败导致整页白屏。
3. 减少小程序首屏网络往返，并稳定首页 DTO。
4. 原有五个接口继续保留，供各模块列表页和迁移期兼容使用。

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 首页 | 首页聚合 | GET | `/api/home` | 否 | `cityCode?` | `banners,categories,heritageProjects,inheritors,products,courses,activities,news,popup` | 首页当前并发5类接口 | 新增 | `banner`、首页运营位及各领域表 | 各区块固定 DTO，允许局部降级 |
| 首页 | 轮播图 | GET | `/api/banners/enable` | 否 | 无 | `id,title,image,linkType,targetId,sort` | 已有同路径 | 复用 | `banner` | 逐步以 `targetId` 替代任意前端路径字符串 |
| 首页 | 全局搜索 | GET | `/api/search` | 否 | `keyword,type?,categoryId?,levelCode?,regionCode?,page,size` | 分类型结果或统一结果项 | 无 | 新增 | 项目、传承人、商品、课程及索引 | 一期先做数据库检索，不引入新搜索引擎 |

## 4. 非遗项目

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 非遗项目 | 项目分类 | GET | `/api/heritage-categories` | 否 | `parentId?` | `id,code,name,icon,children` | 无公开分类接口 | 新增 | `category`（`bizType=HERITAGE`） | API 使用逻辑资源名，物理表复用分类表 |
| 非遗项目 | 非遗级别 | GET | `/api/heritage-levels` | 否 | 无 | `code,name,sort` | 无 | 新增 | `heritage_level` | 国家/省/市/区县级统一编码 |
| 非遗项目 | 项目列表 | GET | `/api/heritage-projects` | 否 | `keyword,categoryId,levelCode,regionCode,page,size,sort` | `id,name,cover,category,level,region,summary` | 已有 `GET /api/heritage-projects` | 改造 | `heritage_project` | 只返回已发布项目；移除管理端 `status` 语义 |
| 非遗项目 | 项目详情 | GET | `/api/heritage-projects/{id}` | 否 | 路径 `id` | 档案、章节、媒体、传承人摘要、关联内容 | 已有同路径 | 改造 | `heritage_project`、媒体、传承人关系 | 增加官方编号/认定来源；不含 AI 内容 |
| 非遗项目 | 热门项目 | GET | `/api/heritage-projects/recommended` | 否 | `limit,regionCode?` | 项目摘要列表 | 已有无分页 `/all` | 改造 | `heritage_project`、首页运营位 | 替代 `/all` 的无界返回 |

## 5. 非遗传承人、资质、作品与关注

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 传承人 | 公开列表 | GET | `/api/inheritors` | 否 | `keyword,projectId,categoryId,levelCode,regionCode,serviceType,page,size` | `id,displayName,portrait,region,projects,serviceTags,followCount` | 已有同路径偏管理查询 | 改造 | `inheritor`、项目/服务关系 | 只返回审核通过且展示中的档案 |
| 传承人 | 公开详情 | GET | `/api/inheritors/{id}` | 否 | `id` | 公开档案、脱敏资质摘要、作品、商品、课程、关注数 | 已有同路径直接返回实体 | 改造 | `inheritor` 及关系表 | 严禁返回 `idCard/phone/fileObjectKey` |
| 传承人 | 作品列表 | GET | `/api/inheritors/{id}/works` | 否 | `page,size,mediaType?` | `id,title,description,mediaType,url,cover,sort` | 单字符串“作品展示” | 新增 | `inheritor_work` | 支持图/视频 |
| 传承人 | 作品详情 | GET | `/api/inheritor-works/{id}` | 否 | `id` | 作品详情、传承人摘要 | 无 | 新增 | `inheritor_work` | 对应作品详情页 |
| 传承人 | 我的认证申请 | GET | `/api/inheritors/me/application` | 是（C端） | 无 | 主申请状态、资质清单、审核记录摘要 | `GET /api/inheritors/my` | 改造 | `inheritor`、资质、审核日志 | 旧路径迁移期兼容 |
| 传承人 | 提交/补充认证 | POST | `/api/inheritors/me/application` | 是（C端） | 档案字段、`qualifications[]` | `applicationId,auditStatus,submittedAt` | `POST /api/inheritors/apply` | 改造 | `inheritor`、`inheritor_qualification` | 敏感文件先走私有上传 |
| 关注 | 关注传承人 | PUT | `/api/inheritors/{id}/follow` | 是（C端） | `id` | `followed,followCount` | 无 | 新增 | `user_follow` | PUT 幂等 |
| 关注 | 取消关注 | DELETE | `/api/inheritors/{id}/follow` | 是（C端） | `id` | `followed=false,followCount` | 无 | 新增 | `user_follow` | DELETE 幂等 |
| 关注 | 我的关注 | GET | `/api/users/me/follows` | 是（C端） | `page,size` | 传承人摘要分页 | 无 | 新增 | `user_follow`、`inheritor` | 对应个人中心“我的关注” |

## 6. 非遗商城、购物车、订单与售后

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 商城 | 商品分类 | GET | `/api/product-categories` | 否 | `parentId?` | 分类树 | 后端有 `category` 但无接口 | 新增 | `category` | 只返回 `bizType=PRODUCT` |
| 商城 | 商品列表 | GET | `/api/products` | 否 | `keyword,categoryId,inheritorId,projectId,productType,page,size,sort` | 商品摘要、价格区间、可售状态 | 已有同路径 | 改造 | `product`、`product_sku` | 只返回已审核且上架商品 |
| 商城 | 商品详情 | GET | `/api/products/{id}` | 否 | `id` | 商品、SKU、规格、资质摘要、履约方式、传承人 | 已有同路径 | 改造 | 商品/SKU/规格/资质关系 | 食品类展示有效资质摘要 |
| 商城 | SKU实时信息 | GET | `/api/skus/{id}` | 否 | `id` | `skuId,specs,price,availableStock,status,version` | 无 | 新增 | `product_sku` | 下单前仍需服务端再次校验 |
| 购物车 | 加入购物车 | POST | `/api/cart/items` | 是（C端） | `skuId,quantity` | 购物车项、汇总数量 | `POST /api/cart` | 改造 | `cart`、`product_sku` | 不再只传 productId |
| 购物车 | 购物车列表 | GET | `/api/cart/items` | 是（C端） | 无 | 有效/失效项、价格、库存、合计 | `GET /api/cart` | 改造 | `cart`、商品/SKU | 服务端返回失效原因 |
| 购物车 | 修改数量/选中 | PATCH | `/api/cart/items/{id}` | 是（C端） | `quantity?,selected?` | 更新后的项和汇总 | 无 | 新增 | `cart` | 数量必须正数并校验上限 |
| 购物车 | 删除购物车项 | DELETE | `/api/cart/items/{id}` | 是（C端） | `id` | `removed=true` | 无 | 新增 | `cart` | 校验项属于当前用户 |
| 订单 | 结算预览 | POST | `/api/orders/preview` | 是（C端） | `items[{skuId,quantity}],addressId?,couponId?,points?` | 价格明细、库存结果、可用券、`checkoutToken` | 无 | 新增 | SKU、地址、优惠券、积分 | 返回短时 token，客户端价格不可信 |
| 订单 | 创建订单 | POST | `/api/orders` | 是（C端） | `checkoutToken,addressId,remark,fulfillment`，头 `Idempotency-Key` | `orderId,orderNo,status,payableAmount,expiredAt` | `POST /api/orders` | 改造 | `order`、明细、库存/状态流水 | 新订单必须是待支付，不能直接设已支付 |
| 订单 | 我的订单 | GET | `/api/orders` | 是（C端） | `status?,page,size` | 当前用户订单分页 | `GET /api/orders/my` | 改造 | `order`、`order_item` | `/my` 作为兼容路径后废弃 |
| 订单 | 订单详情 | GET | `/api/orders/{id}` | 是（C端） | `id` | 订单、明细、地址快照、状态日志、可执行动作 | 已有同路径但未校验归属 | 改造 | 订单及日志 | 必须校验订单属于当前用户 |
| 订单 | 取消订单 | POST | `/api/orders/{id}/cancel` | 是（C端） | `reason`，头 `Idempotency-Key` | 新状态、库存/权益回退结果 | 通用状态修改 | 新增 | 订单、库存、券/积分流水 | 禁止 C 端传任意 status |
| 订单 | 确认收货 | POST | `/api/orders/{id}/confirm-receipt` | 是（C端） | 无，头 `Idempotency-Key` | `status,completedAt` | `PUT /api/orders/{id}/status` | 改造 | 订单、状态日志 | 仅已发货可确认 |
| 订单 | 发起支付 | POST | `/api/orders/{id}/payment` | 是（C端） | `channel`，头 `Idempotency-Key` | 支付准备信息或“未接入”状态 | 当前创建即支付 | 新增/预留 | 订单、支付预留字段 | 真实微信支付必须另行审批和设计回调 |
| 售后 | 申请售后 | POST | `/api/after-sales` | 是（C端） | `orderItemId,type,reason,amount,evidence[]` | `afterSaleId,afterSaleNo,status` | 无 | 新增 | `after_sale`、日志 | 校验可售后金额和期限 |
| 售后 | 售后列表 | GET | `/api/after-sales` | 是（C端） | `status?,page,size` | 售后分页 | 无 | 新增 | `after_sale` | 仅本人数据 |
| 售后 | 售后详情 | GET | `/api/after-sales/{id}` | 是（C端） | `id` | 申请、协商记录、状态、退款摘要 | 无 | 新增 | 售后及日志 | 证据文件鉴权访问 |
| 售后 | 撤销售后 | POST | `/api/after-sales/{id}/cancel` | 是（C端） | `reason?` | 新状态 | 无 | 新增 | 售后及日志 | 仅允许特定状态 |

## 7. 普通活动、手作课程、场次、预约与核销

普通平台活动继续使用 `activity/signup`；收费或可排期的手作体验使用 `course/course_session/reservation`。

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 活动 | 活动列表 | GET | `/api/activities` | 否 | `keyword,categoryId,cityCode,startDate,page,size` | 活动摘要、报名余量 | `GET /api/activities/enable` | 改造 | `activity` | 公开接口只返回可见活动 |
| 活动 | 活动详情 | GET | `/api/activities/{id}` | 否 | `id` | 活动、规则、组织方、报名状态摘要 | 已有同路径 | 改造 | `activity` | 匿名不返回个人报名数据 |
| 活动 | 报名活动 | POST | `/api/activities/{id}/signups` | 是（C端） | `participantCount,remark`，头 `Idempotency-Key` | `signupId,status,remainingQuota` | `POST /api/signups` | 改造 | `signup`、`activity` | 使用事务和原子名额控制 |
| 活动 | 我的活动报名 | GET | `/api/activity-signups` | 是（C端） | `status?,page,size` | 报名分页 | `GET /api/signups/my` | 改造 | `signup` | 与课程预约明确分开 |
| 活动 | 取消报名 | POST | `/api/activity-signups/{id}/cancel` | 是（C端） | `reason?` | 新状态、余量 | `PUT /api/signups/{id}/cancel` | 改造 | `signup`、`activity` | 保持本人校验 |
| 手作课程 | 课程分类 | GET | `/api/course-categories` | 否 | 无 | 分类列表 | 无 | 新增 | `category` | `bizType=COURSE` |
| 手作课程 | 课程列表 | GET | `/api/courses` | 否 | `keyword,categoryId,inheritorId,projectId,serviceMode,cityCode,date?,page,size,sort` | 课程摘要、起价、近期场次 | 无 | 新增 | `course`、`course_session` | 对应课程列表页 |
| 手作课程 | 课程详情 | GET | `/api/courses/{id}` | 否 | `id` | 课程、讲师、项目、规则、可选服务模式、近期场次 | 活动详情只能近似 | 新增 | `course` 及关系 | 不把课程塞入 activity |
| 课程场次 | 场次列表 | GET | `/api/courses/{id}/sessions` | 否 | `startDate,endDate,serviceMode,status?` | `sessionId,startAt,endAt,location,price,capacity,remaining` | 无 | 新增 | `course_session` | 余位为服务端实时值 |
| 课程场次 | 场次详情 | GET | `/api/course-sessions/{id}` | 否 | `id` | 场次、课程摘要、地点、规则、余位、版本 | 无 | 新增 | `course_session` | 预约前重新查询 |
| 预约 | 预约预览 | POST | `/api/reservations/preview` | 是（C端） | `sessionId,participantCount,couponId?,points?` | 价格、余位、规则、联系人默认值、`reservationToken` | 无 | 新增 | 场次、券、积分 | 不产生占位或明确短时占位策略 |
| 预约 | 创建预约 | POST | `/api/reservations` | 是（C端） | `reservationToken,contact,participants,remark`，头 `Idempotency-Key` | `reservationId,reservationNo,status,payableAmount` | `POST /api/signups` 仅可近似 | 新增 | `reservation`、`course_session` | 事务内占用场次名额 |
| 预约 | 我的预约 | GET | `/api/reservations` | 是（C端） | `status?,page,size` | 预约分页、场次摘要、可执行动作 | `GET /api/signups/my` 仅活动 | 新增 | `reservation` | 对应个人中心“我的预约” |
| 预约 | 预约详情 | GET | `/api/reservations/{id}` | 是（C端） | `id` | 预约、场次、参与人、规则、核销展示信息、日志 | 无 | 新增 | 预约、场次、核销凭证 | 仅本人可查 |
| 预约 | 取消预约 | POST | `/api/reservations/{id}/cancel` | 是（C端） | `reason`，头 `Idempotency-Key` | 新状态、费用/权益回退 | 无 | 新增 | 预约、场次、变更日志 | 按取消规则判断 |
| 预约 | 改期预览 | POST | `/api/reservations/{id}/reschedule-preview` | 是（C端） | `targetSessionId` | 差价、规则、目标余位 | 无 | 新增 | 预约、场次 | 不直接改变预约 |
| 预约 | 确认改期 | POST | `/api/reservations/{id}/reschedule` | 是（C端） | `targetSessionId,previewToken`，头 `Idempotency-Key` | 新场次、差价处理、状态 | 无 | 新增 | 预约、场次、变更日志 | 同一事务释放旧名额并占新名额 |
| 核销 | 获取核销展示数据 | GET | `/api/reservations/{id}/verification` | 是（C端） | `id` | 短时二维码 payload、过期时间、核销状态 | 无 | 新增 | `verification_token` | 不返回数据库令牌摘要 |
| 核销 | 核销预约 | POST | `/api/verifications/verify` | 是（工作人员） | `token`，头 `Idempotency-Key` | 预约摘要、核销结果、核销时间 | 无 | 新增 | 核销凭证/记录、预约 | token 放请求体，避免 URL/日志泄露 |
| 核销 | 查询核销结果 | GET | `/api/verifications/{recordId}` | 是（工作人员） | `recordId` | 核销记录、业务摘要 | 无 | 新增 | `verification_record` | 按传承人/运营数据权限校验 |

## 8. 非遗科普知识库与资讯

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 知识库 | 词条分类 | GET | `/api/knowledge-categories` | 否 | 无 | 分类列表 | 无 | 新增 | `category` | `bizType=KNOWLEDGE` |
| 知识库 | 词条列表 | GET | `/api/knowledge-entries` | 否 | `keyword,projectId,categoryId,mediaType?,page,size,sort` | 词条摘要、项目、封面、媒体类型 | `GET /api/news` 只能近似 | 新增 | `knowledge_entry`、媒体 | 结构化知识不与 news 混用 |
| 知识库 | 词条详情 | GET | `/api/knowledge-entries/{id}` | 否 | `id` | 标题、摘要、章节、媒体、来源、关联项目/传承人 | 项目 description 只能近似 | 新增 | 词条、章节、媒体 | 支持图文和短视频 |
| 资讯 | 资讯列表 | GET | `/api/news` | 否 | `keyword,category,page,size,sort` | 资讯摘要分页 | 已有同路径 | 复用/小改 | `news` | 公开端固定 `status=已发布` |
| 资讯 | 资讯详情 | GET | `/api/news/{id}` | 否 | `id` | 资讯正文、来源、发布时间 | 已有同路径 | 复用 | `news` | 不承担科普词条职责 |
| 知识库 | 生成分享素材 | POST | `/api/share-assets` | 是（C端） | `targetType,targetId,template?` | 标题、文案、标签、海报临时URL | 无 | 新增 | 目标内容表；临时产物不新增业务表 | 一期生成素材，不承诺跨平台自动发布 |

## 9. C 端身份与个人中心

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 身份 | 微信登录 | POST | `/api/auth/wechat/login` | 否 | `code,encryptedData?,iv?` | `token,user,isNewUser` | 无，当前是密码登录 | 新增 | `user`、`user_identity` | AppSecret只在服务端环境变量 |
| 身份 | 发送短信验证码 | POST | `/api/auth/sms/codes` | 否 | `phone,purpose` | `requestId,retryAfter` | 无 | 新增 | 短时验证码/审计 | 限流、图形/风控策略另定 |
| 身份 | 短信验证码登录 | POST | `/api/auth/sms/login` | 否 | `phone,code,requestId` | `token,user,isNewUser` | 当前手机号+密码登录 | 新增 | `user`、`user_identity` | 验证码一次性消费 |
| 身份 | 兼容密码登录 | POST | `/api/user/login` | 否 | `phone或username,password` | `token` | 已有同路径 | 改造后逐步废弃 | `user` | 密码升级为自适应哈希 |
| 个人中心 | 我的资料 | GET | `/api/users/me` | 是（C端） | 无 | 用户公开资料、绑定状态 | `GET /api/user/info` | 改造 | `user`、`user_identity` | 旧路径迁移期兼容 |
| 个人中心 | 修改资料 | PATCH | `/api/users/me` | 是（C端） | `nickname,avatar,gender,birthday,email` 可选 | 更新后的用户资料 | `PUT /api/user/info` | 改造 | `user` | 不允许修改身份字段 |
| 个人中心 | 首页摘要 | GET | `/api/users/me/summary` | 是（C端） | 无 | 订单/预约/售后/收藏/关注/券/积分计数 | 前端当前并发多个接口 | 新增 | 用户及各领域表 | 个人中心聚合接口 |
| 个人中心 | 退出当前会话 | POST | `/api/auth/logout` | 是（C端） | 无 | `success` | C端仅本地清理 | 新增 | Token黑名单或会话表（按方案） | JWT纯无状态时至少清本地并轮换策略 |

## 10. 种草笔记、评论与点赞

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 种草笔记 | 笔记列表 | GET | `/api/notes` | 否 | `category,tag,targetType?,targetId?,featured?,page,size,sort` | 作者、正文摘要、媒体、标签、挂载、互动数 | `GET /api/posts` | 改造 | `post` 及媒体/标签/挂载 | `/api/posts` 兼容后废弃 |
| 种草笔记 | 笔记详情 | GET | `/api/notes/{id}` | 否 | `id` | 完整笔记、媒体、标签、挂载、互动状态 | `GET /api/posts/{id}` | 改造 | `post` 及关系 | 登录用户可附加 liked/favorited |
| 种草笔记 | 发布笔记 | POST | `/api/notes` | 是（C端） | `title,content,mediaIds[],tagIds[],targets[]` | `noteId,auditStatus,createdAt` | `POST /api/posts` | 改造 | `post`、媒体、标签、挂载 | 新发布进入审核状态 |
| 种草笔记 | 修改笔记 | PATCH | `/api/notes/{id}` | 是（C端） | 可编辑字段 | 新审核状态、更新时间 | 无 | 新增 | 笔记及关系 | 仅作者；重大修改重新审核 |
| 种草笔记 | 删除笔记 | DELETE | `/api/notes/{id}` | 是（C端） | `id` | `deleted=true` | `DELETE /api/posts/{id}` | 改造 | `post` | 逻辑删除并保留审核记录 |
| 种草笔记 | 点赞 | PUT | `/api/notes/{id}/like` | 是（C端） | `id` | `liked=true,likeCount` | `POST /api/posts/{id}/like` 是切换语义 | 改造 | `post_like` | PUT/DELETE 显式且幂等 |
| 种草笔记 | 取消点赞 | DELETE | `/api/notes/{id}/like` | 是（C端） | `id` | `liked=false,likeCount` | 同上 | 改造 | `post_like` | 不使用 toggle 降低重试歧义 |
| 种草笔记 | 评论列表 | GET | `/api/notes/{id}/comments` | 否 | `page,size,parentId?` | 评论分页、回复摘要 | `GET /api/comments?postId=` | 改造 | `comment` | 路径绑定笔记ID |
| 种草笔记 | 发布评论 | POST | `/api/notes/{id}/comments` | 是（C端） | `content,parentId?,replyToCommentId?` | `commentId,auditStatus` | `POST /api/comments` | 改造 | `comment` | 服务端确定 postId 和当前用户 |
| 种草笔记 | 我的笔记 | GET | `/api/users/me/notes` | 是（C端） | `auditStatus?,page,size` | 笔记分页 | `GET /api/posts/my` | 改造 | `post` | 显示审核失败原因 |
| 种草笔记 | 上传公开媒体 | POST | `/api/media` | 是（C端） | multipart文件、`usage=NOTE` | `uploadToken,type,tempUrl,cover,duration` | `POST /api/upload/image` | 改造 | 发布成功后写 `post_media`；上传阶段使用临时对象存储 | 校验真实 MIME、大小、视频时长；临时 token 有效期受控 |

## 11. 收藏

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 收藏 | 收藏目标 | PUT | `/api/favorites/{type}/{targetId}` | 是（C端） | `type,targetId` | `favorited=true` | `POST /api/favorites/toggle` | 改造 | `favorite` | 类型白名单；PUT幂等 |
| 收藏 | 取消收藏 | DELETE | `/api/favorites/{type}/{targetId}` | 是（C端） | `type,targetId` | `favorited=false` | toggle | 改造 | `favorite` | DELETE幂等 |
| 收藏 | 收藏状态 | GET | `/api/favorites/{type}/{targetId}` | 否/可登录 | 路径参数 | `favorited` | `GET /api/favorites/status` | 改造 | `favorite` | 匿名固定 false |
| 收藏 | 我的收藏 | GET | `/api/favorites` | 是（C端） | `type?,page,size` | 通用目标摘要分页 | `GET /api/favorites/my` | 改造 | `favorite` 及目标表 | 扩展项目、传承人、课程、知识类型 |
| 收藏 | 收藏统计 | GET | `/api/favorites/stats` | 是（C端） | 无 | 各类型数量 | 已有同路径 | 改造 | `favorite` | 类型集合与数据库文档一致 |

## 12. 收货地址

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 地址 | 地址列表 | GET | `/api/addresses` | 是（C端） | 无 | 地址列表、默认标记 | 无，订单仅保存文本 | 新增 | `user_address` | 手机号按场景脱敏 |
| 地址 | 地址详情 | GET | `/api/addresses/{id}` | 是（C端） | `id` | 地址完整字段 | 无 | 新增 | `user_address` | 仅本人 |
| 地址 | 新增地址 | POST | `/api/addresses` | 是（C端） | 收件人、手机号、地区码、详细地址、默认标记 | 新地址 | 无 | 新增 | `user_address` | 设置默认地址需事务更新 |
| 地址 | 修改地址 | PATCH | `/api/addresses/{id}` | 是（C端） | 可编辑字段 | 更新后地址 | 无 | 新增 | `user_address` | 已下单订单快照不随之变化 |
| 地址 | 删除地址 | DELETE | `/api/addresses/{id}` | 是（C端） | `id` | `deleted=true` | 无 | 新增 | `user_address` | 逻辑删除；处理中订单不受影响 |
| 地址 | 设置默认地址 | PUT | `/api/addresses/{id}/default` | 是（C端） | `id` | 默认地址 | 无 | 新增 | `user_address` | PUT幂等 |

## 13. 优惠券

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 优惠券 | 可领取优惠券 | GET | `/api/coupons/available` | 是（C端） | `targetType?,targetId?,page,size` | 券模板、领取状态 | 无 | 新增 | `coupon_template`、`user_coupon` | 只返回生效且有库存的券 |
| 优惠券 | 领取优惠券 | POST | `/api/coupons/{templateId}/claim` | 是（C端） | `templateId`，头 `Idempotency-Key` | `userCouponId,code,status,expiresAt` | 无 | 新增 | 模板、用户券 | 原子扣减发放量 |
| 优惠券 | 我的券包 | GET | `/api/coupons` | 是（C端） | `status?,page,size` | 用户券分页 | 无 | 新增 | `user_coupon` | 状态：可用/已用/过期 |
| 优惠券 | 订单可用券 | GET | `/api/coupons/applicable` | 是（C端） | `checkoutToken` 或 `reservationToken` | 可用券和不可用原因 | 无 | 新增 | 券、订单/预约预览 | 最终优惠仍由服务端计算 |

## 14. 积分

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 积分 | 积分账户 | GET | `/api/points/account` | 是（C端） | 无 | `available,frozen,expiringSoon` | 无 | 新增 | `points_account`、流水 | 不从流水临时全表求和 |
| 积分 | 积分流水 | GET | `/api/points/ledger` | 是（C端） | `bizType?,page,size` | 变动值、余额、来源、时间、过期时间 | 无 | 新增 | `points_ledger` | 流水不可修改/删除 |
| 积分 | 积分规则 | GET | `/api/points/rules` | 否 | 无 | 行为、积分值、每日上限、说明 | 无 | 新增 | `points_rule` | 仅返回当前生效规则 |
| 积分 | 订单可用积分 | GET | `/api/points/applicable` | 是（C端） | `checkoutToken` 或 `reservationToken` | `maxPoints,maxDiscount` | 无 | 新增 | 积分账户/规则 | 实际抵扣在创建订单事务中完成 |

## 15. 运营后台与履约配套接口

以下接口为上述 C 端内容和交易提供运营入口。管理后台页面不属于 C 端路由，但属于一期闭环。所有接口必须是管理员/工作人员权限，不能继续暴露在公共 CRUD 路径。

| 模块 | 接口名称 | Method | URL | 登录 | 请求参数 | 返回核心字段 | 当前类似接口 | 处理方式 | 数据表 | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|
| 后台-项目 | 项目管理列表/CRUD | GET/POST/PATCH | `/api/admin/heritage-projects`；`/api/admin/heritage-projects/{id}` | 是（管理员） | 查询或项目 DTO | 管理字段、审核状态 | 公共路径下已有 CRUD | 改造 | `heritage_project` | 迁出公共 Controller |
| 后台-传承人 | 认证审核 | POST | `/api/admin/inheritors/{id}/audit` | 是（资质审核） | `decision,remark,qualificationDecisions[]` | 审核结果、日志 | `PUT /api/inheritors/{id}/audit` | 改造 | 传承人、资质、审核日志 | 禁止无管理员鉴权调用 |
| 后台-资质 | 查看私有资质 | GET | `/api/admin/qualifications/{id}/access` | 是（资质审核） | `id` | 脱敏信息、短时文件URL | 公开 uploads | 新增 | `inheritor_qualification` | 记录查看审计 |
| 后台-商品 | 商品/SKU审核与库存调整 | POST | `/api/admin/products/{id}/audit` 等 | 是（商品运营） | 决策、SKU、库存调整原因 | 商品/SKU/流水 | 现有商品 CRUD/stock | 改造 | 商品、SKU、库存流水 | 库存调整必须留痕 |
| 后台-商品 | 商家资质管理 | GET/POST/PATCH | `/api/admin/merchant-qualifications`；`/api/admin/merchant-qualifications/{id}` | 是（资质审核） | 主体、资质类型、有效期、私有文件、审核决定 | 资质详情、有效状态 | 无 | 新增 | `merchant_qualification` | 食品类商品上架前强制校验有效资质 |
| 后台-商品 | 运费模板管理 | GET/POST/PATCH | `/api/admin/freight-templates`；`/api/admin/freight-templates/{id}` | 是（商品运营） | 计费方式、区域规则、状态 | 运费模板详情 | 无 | 新增 | `freight_template` | 订单预览按模板计算，不信任前端运费 |
| 后台-课程 | 课程管理/审核 | GET/POST/PATCH | `/api/admin/courses`；`/api/admin/courses/{id}` | 是（课程运营） | 课程 DTO | 课程及审核状态 | 无 | 新增 | `course` | 支撑课程上架 |
| 后台-场次 | 场次排期 | GET/POST/PATCH | `/api/admin/course-sessions`；`/api/admin/course-sessions/{id}` | 是（课程运营） | 时间、地点、容量 | 场次和余位 | 无 | 新增 | `course_session` | 已有预约后限制关键字段修改 |
| 后台-预约 | 预约管理 | GET | `/api/admin/reservations` | 是（履约人员） | 课程/场次/状态/日期分页 | 预约分页 | 现有 signup 管理只能近似 | 新增 | `reservation` | 支持导出但需数据脱敏 |
| 后台-核销 | 核销记录 | GET | `/api/admin/verifications` | 是（履约人员） | 操作人/日期/业务筛选 | 核销记录分页 | 无 | 新增 | `verification_record` | 审计用途 |
| 后台-知识 | 词条管理/审核 | GET/POST/PATCH | `/api/admin/knowledge-entries`；`/api/admin/knowledge-entries/{id}` | 是（内容运营） | 词条、章节、媒体 | 管理详情 | 现有 news CRUD 不能替代 | 新增 | 知识词条/章节/媒体 | news 继续独立管理 |
| 后台-笔记 | 笔记审核/精选 | POST | `/api/admin/notes/{id}/audit` | 是（内容审核） | `decision,reason,featured?` | 审核状态、日志 | 无后台社区模块 | 新增 | `post`、审核日志 | 支持下架与追溯 |
| 后台-售后 | 售后审核/仲裁 | POST | `/api/admin/after-sales/{id}/decision` | 是（售后人员） | 决策、金额、备注、幂等键 | 售后状态、退款摘要 | 无 | 新增 | 售后及日志 | 状态机和金额权限控制 |
| 后台-优惠券 | 券模板管理 | GET/POST/PATCH | `/api/admin/coupon-templates`；`/api/admin/coupon-templates/{id}` | 是（营销运营） | 模板、适用范围、总量、有效期 | 模板详情 | 无 | 新增 | `coupon_template` | 生效后关键规则不可随意改 |
| 后台-积分 | 积分规则/人工调整 | GET/POST | `/api/admin/points/rules`；`/api/admin/points/adjustments` | 是（营销/财务权限） | 规则或调整原因、幂等键 | 规则/流水 | 无 | 新增 | 积分规则、账户、流水 | 人工调整双重审计 |

## 16. 现有接口处理清单

### 16.1 可以直接复用

- `GET /api/banners/enable`：保留公开轮播图查询。
- `GET /api/news`、`GET /api/news/{id}`：继续作为时效资讯接口，但公开列表固定只读已发布数据。
- 现有 `Result<T>` 的 `code/message/data` 外层结构作为一期兼容基线。

### 16.2 必须改造后复用

- `/api/heritage-projects`：补分类、级别、地域筛选和公开 DTO；废弃无界 `/all` 用法。
- `/api/inheritors`：区分公开查询与后台审核，公开 DTO 脱敏。
- `/api/products`：接入 SKU、审核状态、资质和履约信息。
- `/api/cart`：迁移到 `/api/cart/items`，增加改数量和删除。
- `/api/orders`：增加归属校验、待支付状态、库存事务、状态机和幂等。
- `/api/activities`、`/api/signups`：保留普通活动语义，重构名额事务；课程预约使用新对象。
- `/api/posts`：兼容映射到 `/api/notes`，增加媒体、标签、挂载和审核。
- `/api/favorites/toggle`：改为显式 PUT/DELETE，避免重试反转状态。
- `/api/user/info`：迁移到 `/api/users/me`。
- `/api/upload/image`：只保留公开图片用途；笔记媒体和私有资质分别使用受控上传流程。

### 16.3 应停止给 C 端或公共调用

- `GET /api/heritage-projects/all`：无分页，不适合作为完整名录接口。
- `PUT /api/orders/{id}/status`：允许客户端传任意状态，应替换为取消、确认收货等动作接口。
- `PUT /api/products/{id}/stock`：必须迁入管理员库存服务并写库存流水。
- `PUT /api/inheritors/{id}/audit`：必须迁入管理员权限域。
- 公共路径下的商品、资讯、活动、项目、订单 DELETE/POST/PUT 管理接口：迁移到 `/api/admin/**`。
- `POST /api/favorites/toggle`、`POST /api/posts/{id}/like`：切换语义不利于网络重试，应由幂等 PUT/DELETE 替代。

### 16.4 必须新增

- 首页聚合、统一搜索；
- 非遗分类/级别、传承人作品/关注；
- SKU、完整购物车、地址、订单预览/状态动作、售后；
- 课程、场次、预约、改期、核销；
- 科普知识词条；
- 微信/短信身份；
- 笔记媒体/标签/业务挂载；
- 优惠券和积分；
- 管理员权限域下的审核、履约和营销接口。

## 17. 契约冻结与实施顺序

1. 先冻结数据库对象、状态枚举、权限主体和本文件路径命名。
2. 建立统一鉴权、错误码、分页和幂等基础设施。
3. 按数据库设计创建迁移，再实现对应 Service；禁止 Controller 直接跨多个 Mapper 完成事务。
4. 先实现管理员/内容生产接口，再联调 C 端公开页面；交易模块先实现 SKU/库存/订单，再改商城页面。
5. 旧接口保留一个明确兼容期，通过日志统计调用量，调用归零后再移除。
6. 每个 API 的实施任务需补 OpenAPI schema、错误码、示例、单元测试、权限测试和并发/幂等测试。
