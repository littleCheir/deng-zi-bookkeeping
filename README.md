# 个人记账 Web 应用（V1.1）

个人使用的轻量级记账网站：手机浏览器优先，记录收入/支出，首页查看收支汇总与图表。
V1 只初始化一个所有者账号，不开放注册；数据模型从一开始按“每条业务数据归属一个用户”设计。

> 当前进度：**阶段 0 —— 项目初始化与开发环境（工程骨架已完成，等待用户手动验收）**。
> 阶段 1–5 尚未实现。

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

已执行过的迁移文件不要再修改，需要变更时新增版本（例如 `V5__...`）。

## 配置项（全部通过环境变量，仓库内不含任何真实凭据）

`backend/src/main/resources/application.yml` 中的敏感配置留空，由运行环境提供：

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `SPRING_DATASOURCE_URL` | 空 | 例：`jdbc:mysql://localhost:3306/expense_tracker?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` | 空 | MySQL 账号 |
| `SPRING_DATASOURCE_PASSWORD` | 空 | MySQL 密码，**不要**写入 `application.yml` 或提交到 Git |
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
mvn spring-boot:run
```

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

## 阶段 0 已实现

- 后端 Maven 工程、应用入口、分层目录
- 统一响应 `{data, error}`、错误体 `{code, message, details}`、全局异常映射
- MyBatis-Plus 基础配置（分页插件、下划线转驼峰、XML 目录）
- Flyway 迁移 V1–V4（三张表、索引、3 个默认分类）
- 三个实体与 Mapper 骨架（`UserMapper`、`CategoryMapper`、`TransactionMapper`）
- 实体使用 Lombok 注解（`@Data` / `@NoArgsConstructor` / `@AllArgsConstructor`），不手写 getter/setter
- Actuator 健康检查（仅暴露 `health`）
- 前端 Vite + TypeScript 工程、路由骨架、应用壳、样式 token、Axios 实例骨架、占位页与 404 页

## 尚未实现（后续阶段）

认证与 CSRF、分类管理、账单增删改查、首页统计与图表、联调与问题记录。
当前页面统一显示“功能尚未接通”，不展示任何模拟数据。

## 手动验收与交付记录

- 每个阶段的验收步骤与预期结果见 `docs/阶段交付记录/阶段X交付文档.md`。
- 验收由用户手动执行；AI 不运行测试、构建或验证命令，也不声称构建/测试通过。
- 未验证项与已知风险在各阶段交付文档中如实列出。

## 部署

V1 部署（Nginx、HTTPS、systemd 等）不属于当前文档范围。
