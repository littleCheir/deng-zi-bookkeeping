# 个人记账 Web 应用（V1.1）

个人使用的轻量级记账网站：手机浏览器优先，记录收入/支出，首页查看收支汇总与图表。
V1 只初始化一个所有者账号，不开放注册；数据模型从一开始按“每条业务数据归属一个用户”设计。

> 当前进度：**阶段 3 —— 账单记录 CRUD（已验收 2026-10-23）**。
> 阶段 4–5 尚未实现。

## 目录结构

```text
backend/    Spring Boot 3.3 + MyBatis-Plus + Flyway 后端
frontend/   Vue 3 + TypeScript + Vite 前端
docs/       设计文档、12 份阶段文档、阶段交付记录
README.md   本文件
```

## 技术栈

- 后端：Java 21、Spring Boot 3.3.x、Maven、Spring Security（阶段 1 引入）、MyBatis-Plus、Lombok、MySQL 8、Flyway、Actuator
- 前端：Vue 3、TypeScript、Vite、Vue Router、Pinia、Element Plus、Axios、Apache ECharts、dayjs
- 接口：统一前缀 `/api/v1`，JSON 字段 camelCase；金额为十进制字符串；日期为 `YYYY-MM-DD`

## 环境要求

- JDK 21（本机验证版本 21.0.5）
- Maven 3.9.x（本机验证版本 3.9.6；项目**不提供** `mvnw` wrapper）
- MySQL 8.x
- Node.js 18+ 与 npm

## 数据库准备（由用户执行）

本项目不使用 JPA 自动建表，表结构与初始数据全部由 Flyway 迁移脚本创建，但 **Flyway 不会创建数据库本身**，需要先手工建库：

```sql
CREATE DATABASE expense_tracker
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
```

首次启动后端时，Flyway 会按顺序执行：

- `V1__create_sys_user.sql`
- `V2__create_category.sql`
- `V3__create_transaction_record.sql`
- `V4__seed_default_categories.sql`（3 个默认分类：支出「餐饮」「其他」，收入「工资」）
- `V5__category_management_indexes.sql`（分类查询索引与个人分类唯一索引）

已执行过的迁移文件不要再修改，需要变更时新增版本（例如 `V6__...`）。

## 配置项（本地凭据不入库）

### 推荐方式：`application-local.yml`

数据库连接信息放在 `backend/src/main/resources/application-local.yml`。该文件含真实凭据，已被 `.gitignore` 中的 `*-local.yml` 忽略，**不要提交到 Git**。内容示例（把值换成你自己的）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/expense_tracker?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: 你的数据库账号
    password: 你的数据库密码
app:
  initial-owner:
    # 首次启动且该用户名不存在时创建；已存在则不会重置密码
    username: 你的登录用户名
    password: 你的登录密码
```

启动时激活 `local` profile（任选一种）：

```powershell
# 方式一：环境变量
$env:SPRING_PROFILES_ACTIVE = "local"
cd backend
mvn spring-boot:run

# 方式二：Maven 参数
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

在 IDEA 中运行时，把 Run/Debug Configuration 的 `Active profiles` 填为 `local`。

`application.yml` 只保留 `${SPRING_DATASOURCE_*:}` 占位，`application-local.yml` 会覆盖它；生产环境请用环境变量或服务器外部配置，不要把凭据写进仓库。

### 可选：环境变量

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `SPRING_DATASOURCE_URL` | 空 | 不建 `application-local.yml` 时可改用此变量，例：`jdbc:mysql://localhost:3306/expense_tracker?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` | 空 | MySQL 账号 |
| `SPRING_DATASOURCE_PASSWORD` | 空 | MySQL 密码，**不要**写入 `application.yml` 或提交到 Git |
| `SPRING_PROFILES_ACTIVE` | 空 | 设为 `local` 时加载 `application-local.yml` |
| `SERVER_PORT` | `8080` | 后端端口 |
| `APP_INITIAL_OWNER_USERNAME` | 空 | 初始所有者用户名（阶段 1 使用） |
| `APP_INITIAL_OWNER_PASSWORD` | 空 | 初始所有者密码（阶段 1 使用，只存 BCrypt 哈希） |

前端开发配置见 `frontend/.env.development`（不含密钥）：

| 变量 | 默认值 | 说明 |
|---|---|---|
| `VITE_API_BASE_URL` | `/api/v1` | 前端请求前缀 |
| `VITE_PROXY_TARGET` | `http://localhost:8080` | 开发服务器把 `/api` 转发到的后端地址 |
| `VITE_DEV_PORT` | `5173` | 前端开发服务器端口 |

## 启动后端

```powershell
cd backend
# 激活 local profile，加载 application-local.yml 中的数据库连接
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

- 也可先设置 `$env:SPRING_PROFILES_ACTIVE = "local"` 再执行 `mvn spring-boot:run`。
- 首次启动会执行 Flyway 迁移；数据库连接信息未配置时启动会失败，这是预期行为。
- 健康检查：`GET http://localhost:8080/actuator/health`
- 修改端口：设置环境变量 `SERVER_PORT` 后再启动。

## 启动前端

```powershell
cd frontend
npm install
npm run dev
```

- 打开终端输出的本地地址（默认 `http://localhost:5173`）。
- 手机与电脑在同一局域网时，可用 `http://<电脑IP>:5173` 访问；`/api` 会代理到 `VITE_PROXY_TARGET`。
- 构建静态资源：`npm run build`（输出到 `frontend/dist/`）。

## 阶段 0 已实现（已验收 2026-10-22）

- 用户验收结果：前后端可调通，后端 `/actuator/health` 返回 `UP`；数据库连接信息配置在 `application-local.yml`
- 后端 Maven 工程、应用入口、分层目录
- 统一响应 `{data, error}`、错误体 `{code, message, details}`、全局异常映射
- MyBatis-Plus 基础配置（分页插件、下划线转驼峰、XML 目录）
- Flyway 迁移 V1–V4（三张表、索引、3 个默认分类）
- 三个实体与 Mapper 骨架（`UserMapper`、`CategoryMapper`、`TransactionMapper`）
- 实体使用 Lombok 注解（`@Data` / `@NoArgsConstructor` / `@AllArgsConstructor`），不手写 getter/setter
- Actuator 健康检查（仅暴露 `health`）
- 前端 Vite + TypeScript 工程、路由骨架、应用壳、样式 token、Axios 实例骨架、占位页与 404 页

## 阶段 1 已实现（已验收 2026-10-23）

- 用户验收结果：认证、CSRF、登录页与路由守卫均通过；验收中修复了 `CsrfConfigurer.accessDeniedHandler` 编译问题
- 后端认证：服务端 Session + HttpOnly Cookie（有效期 7 天，`SameSite=Lax`），生产 HTTPS 时把 `SESSION_COOKIE_SECURE=true`
- CSRF：Cookie 名 `XSRF-TOKEN`（非 HttpOnly），请求头 `X-XSRF-TOKEN`；写请求必须携带
- 接口：
  - `GET /api/v1/auth/csrf`（匿名）：下发 CSRF Cookie，返回 `{headerName, parameterName}`
  - `POST /api/v1/auth/login`（匿名）：`{username, password}` → `{id, username}`
  - `POST /api/v1/auth/logout`（需登录 + CSRF）：空 JSON `{}`
  - `GET /api/v1/auth/me`（需登录）：`{id, username}`，未登录 401
- 初始所有者：只从 `application-local.yml` 的 `app.initial-owner` 或 `APP_INITIAL_OWNER_USERNAME` / `APP_INITIAL_OWNER_PASSWORD` 读取；仅用户名不存在时创建，密码保存 BCrypt 哈希，启动时不重置密码
- 统一 401/403 JSON：`UNAUTHENTICATED` / `FORBIDDEN`；登录失败统一提示，不泄露用户名是否存在（CSRF 校验失败为 Spring Security 默认 403，前端按状态码提示）
- 前端：极简 `/login`（仅用户名 + 密码，无注册/找回密码）、Pinia auth store、路由守卫（未登录跳登录页并携带回跳地址）、401 统一清理登录态
- 会话固定防护：登录成功后废弃旧会话并创建新会话
- 已知限制：未实现登录失败次数限制（V1 阶段文档未要求，后续可加）

## 阶段 2 已实现（已验收 2026-10-23）

- 用户验收结果：分类查询、创建与重名、编辑与系统分类保护、删除、前端页面均通过；`CATEGORY_IN_USE` 待阶段 3 后端到端复验
- 分类接口（均需登录）：
  - `GET /api/v1/categories?type=EXPENSE|INCOME`：返回系统分类与当前用户个人分类，按 `type, sortOrder, id` 排序
  - `POST /api/v1/categories`：创建个人分类，`{name, type, icon, sortOrder}`，不接收 `userId/isSystem/isActive`
  - `PUT /api/v1/categories/{id}`：仅可修改 `name/icon/sortOrder`，收支类型不可变
  - `DELETE /api/v1/categories/{id}`：仅个人分类；被账单引用返回 409 `CATEGORY_IN_USE`
- 规则：
  - 同一收支类型内，个人分类不得与当前用户可见的系统分类或个人分类重名 → 409 `CATEGORY_NAME_CONFLICT`
  - 系统分类不可编辑/删除 → 403 `FORBIDDEN`；他人分类按 404 处理
  - 分类删除为物理删除，不做级联删除
- 迁移 `V5__category_management_indexes.sql`：新增 `(type, name)` 查询索引与 `(user_id, type, name)` 唯一索引（系统分类 `user_id` 为 NULL，不受唯一约束影响）
- 前端 `/categories`：收入/支出切换、系统/个人分类标识、个人分类新增/编辑/删除、删除二次确认、错误提示
- 已知限制：V1 不提供分类启停接口，`isActive` 始终为 1；`CATEGORY_IN_USE` 的端到端验证依赖阶段 3 的账单接口（当前可手工插入账单数据验证）

## 阶段 3 已实现（已验收 2026-10-23）

- 用户验收结果：新增/编辑、列表与筛选分页、校验、删除、前端页面均通过；验收中修复了负数金额提示顺序问题

- 账单接口（均需登录，所有读写按当前用户隔离）：
  - `POST /api/v1/transactions`：新增账单，`{type, categoryId, amount, transactionDate, note}`；不接受 `userId`
  - `GET /api/v1/transactions`：分页 + 筛选，`page`（1 起）、`size`（默认 20、最大 100）、`startDate`、`endDate`（含首尾）、`type`、`categoryId`；默认 `transactionDate DESC, id DESC`
  - `GET /api/v1/transactions/{id}`：单笔；跨用户或不存在 404
  - `PUT /api/v1/transactions/{id}`：字段与新增一致，重新校验分类归属/启用/类型
  - `DELETE /api/v1/transactions/{id}`：仅删除自己的记录
- 校验：金额 > 0、最多两位小数、适配 `DECIMAL(12,2)`；日期必填、允许未来日期；备注最多 200 字符；`startDate <= endDate`；`size` 最大 100
- 响应：金额为十进制字符串，日期 `YYYY-MM-DD`，`createdAt` 到秒；列表 `data:{items,page,size,total}`
- 前端 `/transactions`：
  - 新增/编辑弹窗：类型、分类（切换类型清空并重载分类）、金额、日期、备注；金额按字符串校验与提交
  - 列表：日期范围、类型、分类筛选，分页，编辑、删除二次确认；删除当前页最后一条时回上一页
  - 金额展示使用字符串千分位格式化，不做浮点计算
- 阶段 2 的 `CATEGORY_IN_USE` 现在可以端到端验证：创建账单后尝试删除其个人分类，预期 409

## 尚未实现（后续阶段）

首页统计与图表、联调与问题记录。
首页仍显示“功能尚未接通”，不展示任何模拟数据。

## 代码注释约定

- 关键类与关键方法使用中文注释说明职责、约束与安全语义。
- 关键逻辑必须写“为什么”，包括认证与 CSRF、会话与权限、用户隔离、金额精度、日期边界、统计口径、异常映射、前端请求封装与路由守卫。
- 不做逐行复述；简单访问器与 Lombok 生成代码不写冗余注释。
- 每个阶段的交付文档会列出本轮补充的关键注释点。

## 手动验收与交付记录

- 每个阶段的验收步骤与预期结果见 `docs/阶段交付记录/阶段X交付文档.md`。
- 验收由用户手动执行；AI 不运行测试、构建或验证命令，也不声称构建/测试通过。
- 未验证项与已知风险在各阶段交付文档中如实列出。

## 部署

V1 部署（Nginx、HTTPS、systemd 等）不属于当前文档范围。
