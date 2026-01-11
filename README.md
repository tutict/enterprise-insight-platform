# 企业智能数据分析 & AI 助手平台（简历 MVP）

一个可直接用于简历展示的企业级数据分析与 AI 助手平台 MVP。

## 架构
- Spring Cloud Alibaba 微服务（Nacos 注册/配置中心）
- API 网关（Spring Cloud Gateway）
- JWT 鉴权 + RBAC（网关规则 + 服务内方法级校验）
- Garfish 微前端（主应用 + 子应用）
- Mock AI Agent 接口便于快速演示

## 后端模块
- `gateway-service`：API 网关与统一路由
- `auth-service`：登录与 JWT 签发
- `metadata-service`：数据集与数据源
- `analysis-service`：指标卡片 + Feign 调用
- `ai-service`：Mock AI Agent
- `common`：公共响应、错误码、权限注解等

## 技术栈
- Java 21
- Spring Boot 3.2 + Spring Cloud 2023 + Spring Cloud Alibaba 2023
- Nacos / MySQL / Redis（Docker Compose）
- Vue 3 + Vite + Garfish

## 快速开始

### 1) 基础设施
```bash
cd enterprise-insight-backend
# 启动 Nacos / MySQL / Redis
# 需要 Docker Desktop
# 如仅需 Nacos，可注释其他服务

# Windows
powershell -Command "docker compose up -d"
```

### 2) 后端服务
```bash
cd enterprise-insight-backend
# 启动网关
mvn -pl gateway-service -am spring-boot:run

# 新开终端启动其他服务
mvn -pl auth-service -am spring-boot:run
mvn -pl metadata-service -am spring-boot:run
mvn -pl analysis-service -am spring-boot:run
mvn -pl ai-service -am spring-boot:run
```

### 3) 前端（Garfish）
```bash
# 主应用
cd enterprise-insight-frontend
npm install
npm run dev

# 数据洞察子应用
cd enterprise-insight-frontend/insight-app
npm install
npm run dev

# AI 助手子应用
cd enterprise-insight-frontend/assistant-app
npm install
npm run dev
```

### 端口
- 主应用：`http://localhost:5173`
- 洞察子应用：`http://localhost:5174`
- 助手子应用：`http://localhost:5175`
- 网关：`http://localhost:8080`

### 鉴权说明
- JWT secret 可通过 `JWT_SECRET` 环境变量覆盖。
- 调用 `POST /api/auth/login` 获取 token，后续请求带上 `Authorization: Bearer <token>`。
- 服务内使用 `@RequireRoles` + `X-User-Roles` 进行方法级权限校验。
- 错误响应包含统一 `code` 字段（见 `ErrorCodes`）。

## 示例接口（经由网关）
- `GET /api/analysis/metrics`
- `GET /api/analysis/datasets`（Feign -> metadata-service，带 fallback）
- `GET /api/metadata/datasets`
- `POST /api/ai/agent/ask`
- `POST /api/auth/login`

## 简历亮点
- 微服务架构：注册发现 + 配置中心 + 网关路由
- JWT + RBAC：网关规则与服务内方法级校验
- OpenFeign 服务调用 + Resilience4j 降级
- Garfish 微前端：主应用编排多子应用

## 可拓展方向
- 多租户隔离与细粒度权限
- 真正的指标存储与 OLAP 查询
- 接入实际大模型（Qwen/DeepSeek/智谱等）
- 观测性：Tracing + Metrics + Logging
