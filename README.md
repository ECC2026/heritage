# 非遗文化互动平台（Heritage）

同城非遗文化互动平台：看传承、学手艺、逛好物、约体验。包含三端代码：`server_code`（Spring Boot 后端）、`client_code`（uni-app 小程序）、`manage_code`（Vue3 管理后台）。

> 本 README 同步自 `hermes/` 目录的最终实现报告（修改时间 2026-08-12 08:53）。

---

# 新版业务生态基础能力 — 实现报告

- 修改时间：2026-08-12 08:53
- 分支：Du
- 模式：长文档形式指令（22 节任务，Phase A–F）

## 修改摘要
- **A 现状扫描**：确认无 product_system / business_service / cooperation 相关代码，category/categoryId 为既有系统保留不动。
- **B 数据库迁移**：新增 `SQL/migrations/V003__business_ecosystem.sql`，5 张新表 + 6 个默认产品体系 + 给 product/activity 加 `product_system_id`；`SQL/seeds/V004__business_ecosystem_test_data.sql` 为 3 个虚构服务 + 4 个场次的测试数据。
- **C 后端**：产品体系 / 业务服务 / 服务场次 / 服务预约 / B端合作 五模块 entity、mapper、service、controller 全部就位；后端通过现有 JWT 体系鉴权（admin 端 `AdminBaseController.requireAdmin`，C 端 `UserAuthUtil` 取 token 内 userId，**不信任前端 userId**）；Booking 用原子条件 UpdateWrapper 占位/回补容量；全局异常经 `GlobalExceptionHandler` 统一转 Result。
- **D C端（client_code）**：服务列表/详情/场次/预约/我的预约 + B端合作 5 个页面，首页"生态服务"入口 + 个人页"我的预约"快捷入口。
- **E 管理后台（manage_code）**：产品体系 / 服务管理（含场次）/ 合作申请 3 个页面 + API 模块 + 路由 + 侧边菜单。
- **F 编译验证**：三项构建全部通过。

## 修改文件
### 后端新增（server_code/src/main/java/com/example/server_code/）
- `common/BizException.java`、`common/GlobalExceptionHandler.java`
- `utils/AdminAuthUtil.java`、`utils/UserAuthUtil.java`
- `controller/AdminBaseController.java`、`AdminProductSystemController.java`、`AdminBusinessServiceController.java`、`AdminCooperationApplicationController.java`、`ProductSystemController.java`、`BusinessServiceController.java`、`BusinessServiceBookingController.java`、`CooperationApplicationController.java`
- `dto/cooperation/CooperationApplicationRequest.java`、`dto/service/ServiceBookingRequest.java`
- `entity/ProductSystem.java`、`BusinessService.java`、`BusinessServiceSchedule.java`、`BusinessServiceBooking.java`、`CooperationApplication.java`
- `mapper/` 下 5 个 Mapper
- `service/ProductSystemService.java`、`BusinessServiceService.java`、`BusinessServiceBookingService.java`、`CooperationApplicationService.java`

### 后端修改
- `entity/Product.java`（+productSystemId、+transient productSystem）
- `entity/Activity.java`（同上）
- `controller/EntityController.java`（商品列表支持 productSystemId 过滤，normalize 补体系名）

### SQL
- `SQL/migrations/V003__business_ecosystem.sql`
- `SQL/seeds/V004__business_ecosystem_test_data.sql`

### C端（client_code）
- 新增 `pages/service/{list,detail,book,my-bookings}.vue`、`pages/cooperation/index.vue`
- 修改 `common/request/api.js`（+9 个接口）、`pages.json`（+5 路由）、`pages/index/index.vue`（生态服务入口）、`pages/profile/index.vue`（我的预约入口）

### 管理后台（manage_code）
- 新增 `src/api/modules/{productSystem,service,cooperation}.js`、`src/views/{product-systems,services,cooperations}/index.vue`
- 修改 `src/api/index.js`、`src/router/index.js`（+3 路由）、`src/layout/index.vue`（+3 菜单）

## 数据库
- `product_system`（6 默认：cultural_creative 文创雅物 / food_culture 美食风物 / utensils 器具器物 / handicraft_experience 手作体验 / wellness 康养陪伴 / folk_performance 民俗演艺）
- `business_service`、`business_service_schedule`、`business_service_booking`（最小预约闭环，无支付/退款/复杂排期）
- `cooperation_application`（status 后端固定 0，前端不可传）
- `product`、`activity` 增 `product_system_id`（category/categoryId 原样保留）

## API
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /api/product-systems | 启用产品体系 |
| GET | /api/services | 服务分页（productSystemId/keyword） |
| GET | /api/services/{id} | 详情+场次 |
| GET | /api/services/{id}/schedules | 有效场次（含余量） |
| POST | /api/services/{id}/bookings | 预约（需登录，userId 取 token） |
| GET | /api/service-bookings/my | 我的预约 |
| POST | /api/service-bookings/{id}/cancel | 取消（所有权校验+容量回补） |
| GET | /api/cooperations/types | 固定 4 类型 |
| POST | /api/cooperations/applications | 提交申请 |
| GET/POST/PUT | /api/admin/product-systems[/{id}[/status]] | 体系管理 |
| GET/POST/PUT | /api/admin/services[/{id}[/status]]、GET/POST /{id}/schedules | 服务+场次管理 |
| GET | /api/admin/cooperation-applications[/{id}]、PUT /{id}/status | 申请管理 |

## 测试
- 后端 `mvn compile`：EXIT=0（本地 Maven 3.9.4）
- 管理后台 `npm run build`：EXIT=0（新 3 个视图均产出 chunk）
- C端 `build:mp-weixin`：EXIT=0（新 5 页面编译通过）
- 数据库迁移与接口实跑：未执行（见已知问题）

## 已知问题
- 本机 MySQL root 密码未知、Redis 未启动，无法应用迁移做运行时联调；建议在有权限的库上执行 V003 + V004 后按上表实测。
- 系统 Maven 3.6.1 过旧，编译用本地缓存 Maven 3.9.4（未改 pom.xml）。
- 服务详情页右上 pill 文案为静态"可预约"（BusinessService 未加 statusText transient，客户端只展示启用服务，故无影响）。

## 下一步
1. 在有权限的 MySQL 执行迁移并跑一轮后端接口冒烟（重点：容量原子扣减、取消回补、token 校验）。
2. 可选：给服务补 statusText/预约数量汇总，或在管理端加服务预约列表。
3. 如需发布，可将本分支改动提交到 `Du` 分支（当前所有改动均为未提交状态）。
