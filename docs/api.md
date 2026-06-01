# 接口文档

本文档记录 FDE 交付工作台当前依赖的核心接口。前端默认通过 Gateway 访问后端：

```text
http://localhost:8080
```

本地调试时也可以直连 orchestrator：

```text
http://localhost:8091
```

## 通用响应结构

大部分接口返回统一结构：

```json
{
  "success": true,
  "message": "ok",
  "data": {},
  "timestamp": "2026-06-01T10:00:00Z"
}
```

前端 `apiRequest` 会自动解包 `data`。如果 `success` 为 `false`，前端会把 `message` 作为错误提示。

## 登录

```http
POST /api/auth/login
Content-Type: application/json
```

请求：

```json
{
  "username": "admin",
  "password": "admin"
}
```

响应：

```json
{
  "success": true,
  "data": {
    "token": "...",
    "expiresAt": "...",
    "roles": ["ADMIN", "ANALYST"]
  }
}
```

后续受保护接口需要携带：

```http
Authorization: Bearer <token>
```

## 编译自然语言需求

```http
POST /api/compiler/compile
Content-Type: application/json
Authorization: Bearer <token>
```

请求：

```json
{
  "requirement": "为客户管理能力增加查询接口和验证用例"
}
```

响应核心字段：

```json
{
  "dsl": {
    "name": "ai-harness-generated-system",
    "type": "spring-boot-backend",
    "requirement": "为客户管理能力增加查询接口和验证用例",
    "modules": ["api", "service", "domain"],
    "constraints": {
      "language": "Java 17+",
      "framework": "Spring Boot 3"
    },
    "outputFormat": "Return complete source files with paths and code blocks"
  },
  "prompt": "ROLE\n..."
}
```

## 从 Graph 编译 Prompt

```http
POST /api/compiler/from-graph
Content-Type: application/json
Authorization: Bearer <token>
```

用途：

- 将可视化 Graph Definition 转为 DSL flow。
- 将 DSL flow 编译为 Harness Prompt。
- 用于 Playbooks 页面中的“生成 Prompt”能力。

## 查询当前项目清单

```http
GET /api/project-analysis/current
Authorization: Bearer <token>
```

用途：

- 扫描当前项目根目录。
- 识别模块、API、前端路由、文档、测试。
- 聚合业务能力候选和交付机会。

配置项：

```yaml
project:
  analysis:
    root: ${EIP_PROJECT_ANALYSIS_ROOT:}
    max-depth: 8
    max-files: 5000
    max-evidence-per-category: 80
```

响应示例：

```json
{
  "rootPath": "C:/repo/enterprise-insight-platform",
  "generatedAt": "2026-06-01T10:00:00Z",
  "summary": {
    "scannedFiles": 500,
    "moduleCount": 8,
    "apiEndpointCount": 42,
    "frontendRouteCount": 6,
    "businessCapabilityCount": 12,
    "documentCount": 9,
    "testCount": 30
  },
  "modules": [
    {
      "name": "backend/orchestrator-service",
      "path": "backend/orchestrator-service",
      "type": "java-service",
      "fileCount": 120,
      "markers": ["pom.xml", "src"]
    }
  ],
  "apiEndpoints": [
    {
      "method": "GET",
      "path": "/api/project-analysis/current",
      "sourcePath": "backend/orchestrator-service/src/main/java/.../ProjectAnalysisController.java",
      "line": 19
    }
  ],
  "frontendRoutes": [
    {
      "path": "/project",
      "sourcePath": "enterprise-insight-backend-react/src/App.tsx",
      "line": 28
    }
  ],
  "businessCapabilities": [
    {
      "name": "project analysis",
      "category": "business-signal",
      "evidenceCount": 3,
      "evidence": []
    }
  ],
  "documents": [],
  "tests": [],
  "deliveryOpportunities": []
}
```

## 生成当前项目交付简报

```http
GET /api/project-analysis/current/delivery-brief
Authorization: Bearer <token>
```

用途：

- 基于项目清单生成可直接运行的 FDE requirement。
- 把业务能力候选、API、路由、文档、测试和交付机会合并为证据化输入。
- 给 Delivery Run 页面提供默认目标目录、验证命令和修复轮次。

响应示例：

```json
{
  "title": "Anchor FDE discovery on project analysis",
  "summary": "This capability has the strongest code evidence.",
  "requirement": "FDE delivery brief: ...",
  "playbookId": "industry-business-discovery",
  "playbookName": "Industry And Business Discovery",
  "targetDirectory": "generated-fde-delivery",
  "verifyCommands": [["mvn", "test"]],
  "maxRepairRounds": 2,
  "options": {
    "source": "project-analysis",
    "projectRoot": "C:/repo/enterprise-insight-platform",
    "opportunityPriority": "P0"
  },
  "evidence": [
    {
      "kind": "api",
      "name": "GET /api/project-analysis/current",
      "sourcePath": "backend/orchestrator-service/src/main/java/.../ProjectAnalysisController.java",
      "line": 19
    }
  ],
  "generatedAt": "2026-06-01T10:00:00Z"
}
```

前端行为：

- Project Intel 页面加载该接口。
- 点击“装载到交付运行”后，`requirement` 写入运行页输入区。
- `targetDirectory`、第一条 `verifyCommands`、`maxRepairRounds` 会带入运行页表单。

## 查询 Playbook 模板

```http
GET /api/graph/playbooks
Authorization: Bearer <token>
```

当前内置模板：

| Playbook ID | 用途 |
| --- | --- |
| `compile-generate-verify-repair` | 默认交付闭环。 |
| `industry-business-discovery` | 行业资料、业务分析、领域建模和交付 backlog。 |

响应示例：

```json
[
  {
    "id": "compile-generate-verify-repair",
    "name": "Compile Generate Verify Repair",
    "description": "Default FDE delivery playbook...",
    "graph": {},
    "defaultRunConfig": {
      "model": "llama3.1",
      "targetDirectory": "generated-fde-delivery",
      "verifyCommands": [["mvn", "test"]],
      "maxRepairRounds": 2
    },
    "evidence": [
      "compiled DSL",
      "harness prompt",
      "runtime events",
      "generated files",
      "verification result"
    ]
  }
]
```

## 编译 Graph

```http
POST /api/graph/compile
Content-Type: application/json
Authorization: Bearer <token>
```

用途：

- 校验 Graph 结构。
- 规范化节点和边。
- 返回错误与警告。

关键规则：

- 必须且只能有一个 `start` 节点。
- 至少有一个 `end` 节点。
- `condition` 节点必须有成功和失败分支。
- 移除带 `maxIterations` 的循环边后，图必须是 DAG。

## 运行 Graph

```http
POST /api/graph/run
Content-Type: application/json
Authorization: Bearer <token>
```

如果请求体不包含 `graph`，后端会使用默认 Playbook 图。

响应：

```json
{
  "success": true,
  "data": {
    "runId": "...",
    "graph": {}
  }
}
```

## Graph SSE 事件流

```http
GET /api/graph/run/stream/{runId}
Accept: text/event-stream
Authorization: Bearer <token>
```

事件类型：

```text
GRAPH_RUN_STARTED
NODE_STARTED
NODE_SUCCEEDED
NODE_FAILED
EDGE_TRAVERSED
GRAPH_RUN_COMPLETED
GRAPH_RUN_FAILED
```

## 启动交付运行

```http
POST /api/orchestrator/run/start
Content-Type: application/json
Authorization: Bearer <token>
```

请求：

```json
{
  "runId": "delivery-run-1",
  "requirement": "FDE delivery brief: ...",
  "model": "llama3.1",
  "targetDirectory": "generated-fde-delivery",
  "verifyCommands": [["mvn", "test"]],
  "maxRepairRounds": 2,
  "options": {
    "source": "project-analysis"
  }
}
```

响应：

```json
{
  "success": true,
  "data": {
    "runId": "delivery-run-1"
  }
}
```

启动后，后端会创建 `DeliveryRunRecord` 并异步执行：

```text
compile -> generate -> verify -> repair
```

## 交付运行 SSE 事件流

```http
GET /api/orchestrator/run/stream/{runId}
Accept: text/event-stream
Authorization: Bearer <token>
```

事件类型：

```text
RUN_REQUESTED
STEP_STARTED
STEP_SUCCEEDED
STEP_FAILED
RUN_COMPLETED
RUN_FAILED
RUN_CANCELLED
RUN_PAUSED
RUN_RESUMED
STEP_RETRY_REQUESTED
```

终态事件 `RUN_COMPLETED` 或 `RUN_FAILED` 的 payload 会包含最终 `OrchestratorRunResponse`，服务端会把它写回对应的 DeliveryRun 记录。

## 控制交付运行

```http
POST /api/orchestrator/run/control
Content-Type: application/json
Authorization: Bearer <token>
```

请求示例：

```json
{
  "type": "PAUSE",
  "runId": "delivery-run-1"
}
```

支持的 `type`：

- `PAUSE`
- `RESUME`
- `CANCEL`
- `RETRY_STEP`

`RETRY_STEP` 需要附带 `step`。

## 同步运行编排

```http
POST /api/orchestrator/run
Content-Type: application/json
Authorization: Bearer <token>
```

该接口保留给同步调用场景。前端 Delivery Run 页面优先使用 `/api/orchestrator/run/start` 和 SSE。

## 查询交付运行证据

```http
GET /api/orchestrator/delivery-runs
Authorization: Bearer <token>
```

用途：

- 返回服务端持久化的交付运行记录列表。
- 当前实现使用 JSON 文件存储。
- 默认路径为 `runtime-logs/delivery-runs`。

详情：

```http
GET /api/orchestrator/delivery-runs/{runId}
Authorization: Bearer <token>
```

响应示例：

```json
{
  "runId": "delivery-run-1",
  "workspaceId": "demo-workspace",
  "playbookId": "compile-generate-verify-repair",
  "playbookName": "Compile Generate Verify Repair",
  "status": "COMPLETED",
  "request": {},
  "response": {},
  "events": [],
  "createdAt": "2026-06-01T10:00:00Z",
  "updatedAt": "2026-06-01T10:01:00Z"
}
```

字段说明：

| 字段 | 含义 |
| --- | --- |
| `runId` | 交付运行 ID。 |
| `workspaceId` | 工作区 ID，当前默认是 `demo-workspace`。 |
| `playbookId` | 使用的 Playbook ID。 |
| `playbookName` | 使用的 Playbook 名称。 |
| `status` | 运行状态：`REQUESTED`、`RUNNING`、`COMPLETED`、`FAILED`、`CANCELLED`。 |
| `request` | 原始运行请求。 |
| `response` | 终态运行响应。 |
| `events` | 运行事件流，可用于还原执行时间线。 |
| `createdAt` | 记录创建时间。 |
| `updatedAt` | 最近更新时间。 |
