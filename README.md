# Enterprise Insight Platform

Enterprise Insight Platform 当前定位为 **FDE 交付工作台**。它面向 Forward Deployed Engineer 在客户现场或企业内部落地 AI 工程交付的场景，把行业资料、业务分析、现有项目代码理解、交付 Playbook、Agent 执行、验证修复和交付证据串成一条可审计、可复现的工程链路。

这个项目不是普通聊天应用，也不是单次代码生成器。它的目标是让 AI 能够先理解业务和既有系统，再把工作落到最急需的业务能力上，并把每一次交付运行沉淀为服务端记录。

## 核心目标

- 用结构化 DSL 和 Playbook 把模糊需求变成可执行交付流程。
- 用项目扫描能力理解现有仓库中的模块、API、前端路由、文档和测试。
- 用交付简报把代码证据转成可直接运行的 FDE 输入。
- 用 `compile -> generate -> verify -> repair` 闭环提升生成结果的可控性。
- 用 `DeliveryRun` 服务端持久化保存输入、事件、输出、验证结果和状态。
- 用 Run Evidence 页面支持复盘、审计和复现。

## FDE 工作流程

```text
行业资料与客户上下文
  -> 业务能力分析
  -> 现有项目代码扫描
  -> 项目理解与交付机会识别
  -> 生成可运行交付简报
  -> 装载到 Delivery Run
  -> 编译 DSL / 生成 Harness Prompt
  -> Agent 生成或修改项目文件
  -> 执行验证命令
  -> 自动修复
  -> 持久化 DeliveryRun 证据
```

## 当前已落地能力

| 能力 | 说明 |
| --- | --- |
| FDE 品牌与信息架构 | 前端已收束为 FDE Delivery Workbench、Delivery Run、Run Evidence、Project Intel、Playbooks。 |
| 项目理解 | `ProjectScannerService` 可以扫描当前仓库，识别模块、API、前端路由、Markdown 文档、测试和业务能力候选。 |
| 交付简报 | `GET /api/project-analysis/current/delivery-brief` 会基于代码证据生成可直接装载到运行页的 requirement。 |
| Playbook 模板 | 已内置 `compile-generate-verify-repair` 和 `industry-business-discovery` 两个 Playbook。 |
| 交付运行 | `/api/orchestrator/run/start` 启动异步运行，SSE 推送运行事件。 |
| 运行证据 | `DeliveryRunStore` 将运行请求、事件、最终响应和状态写入 `runtime-logs/delivery-runs/*.json`。 |
| 前端装载 | Project Intel 页面可以一键把交付简报装载到 Delivery Run 页面。 |
| 验证与修复 | Agent Adapter 支持文件写入、验证命令执行和自动修复轮次。 |

## 架构总览

```text
React FDE Workbench
  |-- Project Intel：项目扫描、能力候选、交付机会、交付简报
  |-- Playbooks：可复用交付流程模板
  |-- Delivery Run：运行 compile/generate/verify/repair 闭环
  |-- Run Evidence：查看服务端持久化运行记录
  |
Gateway Service :8080
  |-- JWT 鉴权
  |-- API 路由
  |
  +--> Harness Compiler
  |      |-- Requirement -> DSL
  |      |-- Graph -> DSL
  |      '-- DSL -> Harness Prompt
  |
  +--> Orchestrator Service
  |      |-- ProjectScannerService
  |      |-- ProjectAnalysisController
  |      |-- PlaybookTemplateService
  |      |-- RunExecutionEngine
  |      |-- RunEventStreamService
  |      '-- DeliveryRunStore
  |
  '--> Agent Adapter
         |-- LLM Provider Adapter
         |-- Project File Writer
         |-- Verification Command Runner
         '-- Auto Repair Loop
```

## 关键接口

| 接口 | 用途 |
| --- | --- |
| `POST /api/auth/login` | 登录并获取 JWT。 |
| `POST /api/compiler/compile` | 将自然语言需求编译为 DSL 和 Prompt。 |
| `POST /api/compiler/from-graph` | 将 Graph Definition 编译为 DSL 和 Prompt。 |
| `GET /api/project-analysis/current` | 扫描当前项目并返回项目清单。 |
| `GET /api/project-analysis/current/delivery-brief` | 基于项目清单生成可运行交付简报。 |
| `GET /api/graph/playbooks` | 查询内置 Playbook 模板。 |
| `POST /api/orchestrator/run/start` | 启动异步交付运行。 |
| `GET /api/orchestrator/run/stream/{runId}` | 订阅交付运行 SSE 事件。 |
| `GET /api/orchestrator/delivery-runs` | 查询服务端持久化的交付运行记录。 |

更完整的接口说明见 [docs/api.md](docs/api.md)。

## 快速启动

环境要求：

- Java 21+
- Maven 3.9+
- Node.js 20+
- npm
- Docker Desktop 或 Docker Engine，按需启动中间件
- Ollama，可选，用于本地模型执行

后端测试：

```powershell
cd backend
mvn test
```

前端开发服务：

```powershell
cd enterprise-insight-backend-react
npm install
npm run dev -- --host 127.0.0.1
```

默认访问地址：

```text
前端工作台        http://127.0.0.1:5173
Gateway API       http://localhost:8080
Orchestrator API  http://localhost:8091
```

登录账号：

```text
用户名：admin
密码：admin
```

## 本地验证命令

```powershell
cd backend
mvn -q -pl orchestrator-service -am test
```

```powershell
cd enterprise-insight-backend-react
npm run build
npm run lint
```

## 文档入口

- [FDE 交付工作台路线图](docs/fde-tooling-roadmap.md)
- [架构说明](docs/architecture.zh-CN.md)
- [接口文档](docs/api.md)
- [开发指南](docs/development.md)
- [安全测试说明](docs/security-testing.md)

## 后续重点

1. 将 `DeliveryRun` 从 JSON 文件持久化升级为 H2/PostgreSQL repository。
2. 为每次运行导出 Markdown/JSON 交付证据包。
3. 将项目扫描结果接入 Discovery Playbook 的运行上下文。
4. 增加工作区模型，把客户、项目、仓库、运行记录和证据包挂到同一个 Workspace 下。
5. 扩展更多 FDE Playbook，例如 API 集成原型、遗留系统现代化评估、缺陷复现与修复。

## License

MIT
