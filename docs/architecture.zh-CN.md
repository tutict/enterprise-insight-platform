# 架构说明

## 设计原则

FDE 交付工作台把 Prompt Engineering 看成工程编译和运行过程。系统不会直接把一句自然语言丢给模型生成代码，而是通过项目理解、DSL、Prompt 编译、Playbook、运行事件、验证命令和交付记录建立可控链路。

核心原则：

- 先理解业务和现有项目，再执行生成或修改。
- 分离“要解决什么问题”和“如何执行生成”。
- 每次运行都必须可追踪、可验证、可复现。
- 前端只负责配置、展示和交互，执行逻辑保留在后端。
- 所有模型输入、生成内容和验证命令都必须按不可信输入处理。

## 总体分层

```text
前端工作台
  -> Gateway 鉴权与路由
  -> 项目理解 / Playbook / 编译 / 运行 API
  -> Agent 执行与验证
  -> DeliveryRun 持久化
```

### 1. 前端层

位置：`enterprise-insight-backend-react`

职责：

- 登录和路由保护。
- DSL 编辑。
- Playbook 加载和 Graph Builder 展示。
- Project Intel 项目理解页面。
- Delivery Run 运行页。
- Run Evidence 运行证据页面。
- SSE 事件消费和运行状态渲染。

前端不负责：

- Graph 执行。
- 文件写入。
- 验证命令执行。
- 自动修复。
- DeliveryRun 持久化。

### 2. Gateway 层

位置：`backend/gateway-service`

职责：

- JWT 鉴权。
- 角色校验。
- `/api/**` 路由转发。
- 统一保护 compiler、graph、orchestrator、agent、metadata 等接口。

### 3. Harness Compiler 层

位置：`backend/harness-compiler`

职责：

- 将自然语言需求编译为 DSL。
- 将 Graph Definition 编译为 DSL flow。
- 将 DSL 编译为 Harness Prompt。
- 对 Prompt 输入做基本隔离和注入防护。

关键对象：

- `DslModel`
- `CompileResponse`
- `RuleBasedDslParser`
- `GraphToDslCompiler`
- `PromptCompiler`

### 4. Orchestrator 层

位置：`backend/orchestrator-service`

职责：

- 项目扫描和交付简报生成。
- Playbook 模板提供。
- 运行请求接收。
- 异步运行执行。
- SSE 运行事件推送。
- DeliveryRun 服务端持久化。
- Graph 编译、校验和执行。

关键模块：

```text
project/   项目扫描、业务能力候选、交付机会和交付简报
delivery/  DeliveryRun 记录、状态和文件持久化
runtime/   Delivery Run 异步执行、SSE、暂停、恢复、取消、重试
graph/     Graph 模型、Playbook 模板、图编译和图运行
service/   同步编排入口
```

### 5. Agent Adapter 层

位置：`backend/agent-adapter`

职责：

- 调用模型 Provider。
- 解析模型输出中的文件块。
- 将文件写入受控输出目录。
- 执行验证命令。
- 基于验证失败结果触发自动修复。

安全边界：

- 生成文件不得逃逸输出根目录。
- 验证命令必须经过白名单控制。
- shell 控制符、路径穿越和危险执行器会被拒绝。

## FDE 运行主链路

```text
Project Intel
  -> GET /api/project-analysis/current
  -> GET /api/project-analysis/current/delivery-brief
  -> 装载到 Delivery Run
  -> POST /api/orchestrator/run/start
  -> GET /api/orchestrator/run/stream/{runId}
  -> DeliveryRunStore 写入运行证据
  -> Run Evidence 查询复盘
```

## 项目理解子系统

### 输入

- 当前仓库根目录。
- `project.analysis.root` 配置。
- `EIP_PROJECT_ANALYSIS_ROOT` 环境变量。

### 扫描内容

- Maven / npm 模块。
- Java Controller 映射。
- React 路由。
- Markdown 文档。
- 测试文件。

### 输出

- `ProjectInventory`：完整项目清单。
- `BusinessCapability`：业务能力候选。
- `DeliveryOpportunity`：交付机会。
- `ProjectDeliveryBrief`：可运行交付简报。

### 价值

项目理解让 AI 不再只基于用户输入猜测，而是可以把现有代码中的 API、路由、文档和测试作为证据，生成更贴近真实业务和系统结构的交付输入。

## DeliveryRun 子系统

### 创建

`POST /api/orchestrator/run/start` 接收运行请求后：

1. 生成或复用 runId。
2. 创建 `DeliveryRunRecord`。
3. 持久化初始请求。
4. 异步启动执行引擎。

### 事件

运行事件包括：

- `RUN_REQUESTED`
- `STEP_STARTED`
- `STEP_SUCCEEDED`
- `STEP_FAILED`
- `RUN_COMPLETED`
- `RUN_FAILED`
- `RUN_CANCELLED`
- `RUN_PAUSED`
- `RUN_RESUMED`
- `STEP_RETRY_REQUESTED`

### 持久化

当前第一阶段使用 JSON 文件：

```text
runtime-logs/delivery-runs/{runId}.json
```

记录内容：

- runId
- workspaceId
- playbookId
- playbookName
- status
- request
- response
- events
- createdAt
- updatedAt

## Playbook 子系统

当前内置模板：

| Playbook | 用途 |
| --- | --- |
| `compile-generate-verify-repair` | 默认交付闭环，从需求编译到生成、验证和修复。 |
| `industry-business-discovery` | 行业资料、业务分析、领域建模和交付 backlog。 |

Playbook 由后端提供，前端负责加载和展示。后续应把 Playbook 从代码内置升级为可版本化、可审计的配置资产。

## 配置项

项目扫描配置：

```yaml
project:
  analysis:
    root: ${EIP_PROJECT_ANALYSIS_ROOT:}
    max-depth: 8
    max-files: 5000
    max-evidence-per-category: 80
```

DeliveryRun 持久化配置：

```yaml
delivery:
  run-store:
    storage-root: ${EIP_DELIVERY_RUN_STORAGE_ROOT:runtime-logs/delivery-runs}
    max-list-size: 100
```

## 后续架构演进

1. 引入 `Workspace` 聚合根，把客户、仓库、项目清单、Playbook、DeliveryRun 和证据包挂载到同一上下文。
2. 抽象 `DeliveryRunRepository`，从 JSON 文件迁移到 H2/PostgreSQL。
3. 增加 `RunArtifact` 和 `VerificationLog`，让生成文件、diff 和验证日志可以独立检索。
4. 增加项目依赖图和变更影响分析，让交付机会排序更精确。
5. 增加 Playbook 版本管理，支持模板审批、回滚和运行时追踪。
