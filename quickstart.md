# Quickstart — 本地启动前后端

三端目录：

| 目录 | 说明 | 技术栈 |
|---|---|---|
| `server_code` | 后端 API | Spring Boot 3.5.13 / Java 17 / Maven / MyBatis-Plus / MySQL / Redis / JWT |
| `manage_code` | 管理后台 | Vue 3 + Vite + Element Plus + Pinia + Axios |
| `client_code` | C端小程序 | uni-app (Vue 3) + 微信小程序 (mp-weixin) |

---

## 0. 环境要求

- JDK 17
- **Maven 3.9.4+**：系统自带 Maven 3.6.1 过旧（编译插件需 ≥3.6.3），请用项目自带的 `mvnw`，或本机缓存的 3.9.4：
  ```
  /c/Users/love——ccc/.m2/wrapper/dists/apache-maven-3.9.4-bin/2vqnav6ufo1qvo5j2um40861m/apache-maven-3.9.4/bin/mvn
  ```
- Node.js 18+ / npm
- MySQL 8.x（本机：root / 1234）
- Redis（localhost:6379，无密码）——后端启动必需
- 微信开发者工具（运行 C端小程序用）

---

## 1. 数据库

默认连接 `localhost:3306/heritage`，账号 root，密码通过环境变量或配置文件注入。

### 1.1 创建库与迁移

```bash
mysql -uroot -p1234 -e "CREATE DATABASE IF NOT EXISTS heritage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 按版本顺序执行迁移
mysql -uroot -p1234 heritage < SQL/migrations/V002__phase1_home_foundation.sql
mysql -uroot -p1234 heritage < SQL/migrations/V003__business_ecosystem.sql

# 可选：测试数据
mysql -uroot -p1234 heritage < SQL/seeds/V003__phase1_home_test_data.sql
mysql -uroot -p1234 heritage < SQL/seeds/V004__business_ecosystem_test_data.sql
```

> ⚠️ 仓库内没有 ICHIP 基线建表脚本（无 V001）。`V002` 依赖基线表（如 `category`、`product`、`user` 等）。若库为空，需要先从原始项目导出基线 schema 后，再执行上述迁移，否则 V002 会在引用不存在的表时报错。

---

## 2. 后端（server_code）

端口 `8080`，API 前缀 `/api`。

### 2.1 配置

方式 A：复制示例配置文件并改密码（`spring.profiles.active=local` 已启用）：

```bash
cd server_code/src/main/resources
cp application-local.example.properties application-local.properties
# 编辑 application-local.properties：
#   spring.datasource.password=1234
#   jwt.secret=<至少32字节的随机串>
```

方式 B：直接传环境变量（`application.properties` 支持占位符）：

```bash
DB_URL=jdbc:mysql://localhost:3306/heritage?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true \
DB_USERNAME=root \
DB_PASSWORD=1234 \
JWT_SECRET=<至少32字节的随机串> \
mvn spring-boot:run
```

> `jwt.secret` 为必填，不设会启动失败。

### 2.2 编译与启动

```bash
cd server_code

# 用本机缓存的 3.9.4（推荐）或 ./mvnw
/c/Users/love——ccc/.m2/wrapper/dists/apache-maven-3.9.4-bin/2vqnav6ufo1qvo5j2um40861m/apache-maven-3.9.4/bin/mvn -q compile     # 只编译验证
/c/Users/love——ccc/.m2/wrapper/dists/apache-maven-3.9.4-bin/2vqnav6ufo1qvo5j2um40861m/apache-maven-3.9.4/bin/mvn spring-boot:run # 启动
```

启动成功后：`http://localhost:8080/api/...` 可访问。

### 2.3 冒烟验证

```bash
curl http://localhost:8080/api/product-systems     # 启用中的产品体系
curl http://localhost:8080/api/cooperations/types   # B端合作类型
```

> 管理端接口（`/api/admin/**`）需要登录后携带 `Authorization: Bearer <token>`。

---

## 3. 管理后台（manage_code）

Vue3 + Vite，默认开发端口 `5173`。

```bash
cd manage_code

# 首次：配置 API 地址（默认 http://localhost:8080/api）
cp .env.example .env

npm install
npm run dev      # 启动开发服务器
# 或
npm run build    # 生产构建到 dist/
```

浏览器打开 Vite 输出的地址（默认 `http://localhost:5173`），用后端管理员账号登录。

---

## 4. C端小程序（client_code）

uni-app (Vue 3)，目标为微信小程序。

```bash
cd client_code

npm install

# 开发模式：增量编译并监听
npm run dev:mp-weixin

# 生产构建
npm run build:mp-weixin
```

然后在**微信开发者工具**中导入生成的目录：

- 开发模式：`client_code/unpackage/dist/dev/mp-weixin`
- 生产构建：`client_code/unpackage/dist/build/mp-weixin`

> 小程序 API 地址在 `client_code/common/config.js`：默认 `http://localhost:8080/api`（可用环境变量 `VITE_API_BASE_URL` 覆盖）。开发模式下需在微信开发者工具勾选「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」，localhost 才能直连后端。

---

## 5. 推荐启动顺序

1. 启动 MySQL 与 Redis。
2. 初始化 `heritage` 库（基线 + V002/V003，可选 V004 测试数据）。
3. 启动后端（8080）。
4. 启动管理后台（5173），登录验证 `/api/admin/**`。
5. 运行小程序 dev 模式并导入微信开发者工具，验证 C端页面与预约/合作流程。

## 6. 常见问题

- **`mvn compile` 报「maven-compiler-plugin requires Maven 3.6.3」**：系统 Maven 过旧，改用 `./mvnw` 或本机缓存的 3.9.4（见 §2.2）。
- **后端启动失败：无法连接 Redis**：先启动本地 Redis（默认 6379）。
- **后端启动失败：`jwt.secret` 为空**：设置 `JWT_SECRET` 环境变量或在 `application-local.properties` 中配置。
- **V002 迁移报「Table 'heritage.category' doesn't exist」**：缺 ICHIP 基线 schema，需先导入基线表。
- **小程序请求后端不通**：确认后端已启动、开发者工具已勾选「不校验合法域名」，且 API 地址指向 `http://localhost:8080/api`。
