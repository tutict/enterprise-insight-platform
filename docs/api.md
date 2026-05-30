# 接口文档

本文档记录本地开发和前端控制台当前依赖的核心接口。

## 基础地址

通过 Gateway 访问：

```text
http://localhost:8080
```

本地直连 orchestrator：

```text
http://localhost:8091
```

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

## 编译需求

```http
POST /api/compiler/compile
Content-Type: application/json
Authorization: Bearer <token>
```

请求：

```json
{
  "requirement": "Build a Spring Boot login system with user management and database persistence"
}
```

响应：

```json
{
  "dsl": {
    "name": "ai-harness-generated-system",
    "type": "spring-boot-backend",
    "requirement": "Build a Spring Boot login system with user management and database persistence",
    "modules": ["api", "service", "domain", "authentication", "persistence"],
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

该接口将可视化 Graph Definition 转换为 DSL flow，再生成 Harness Prompt。

## 启动交付运行

前端 Delivery Run 页面使用异步运行接口，先创建 runId，再通过 SSE 消费运行事件。

```http
POST /api/orchestrator/run/start
Content-Type: application/json
Authorization: Bearer <token>
```

请求：

```json
{
  "runId": "delivery-run-1",
  "requirement": "Build a Spring Boot login system with user management and database persistence",
  "model": "llama3.1",
  "targetDirectory": "generated-fde-delivery",
  "verifyCommands": [["mvn", "test"]],
  "maxRepairRounds": 2
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

启动后，编排服务会创建一条 `DeliveryRun` 记录，并将后续运行事件写入服务端持久化存储。

## 交付运行事件流

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

终态事件 `RUN_COMPLETED` 或 `RUN_FAILED` 的 payload 会包含最终 `OrchestratorRunResponse`，服务端会把它写回对应的 `DeliveryRun` 记录。

## 同步运行编排

```http
POST /api/orchestrator/run
Content-Type: application/json
Authorization: Bearer <token>
```

该接口保留给同步调用场景，返回完整编排响应。前端交付运行页面优先使用 `/api/orchestrator/run/start` 与 SSE。

请求：

```json
{
  "requirement": "Build a Spring Boot login system with user management and database persistence",
  "model": "llama3.1",
  "targetDirectory": "generated-harness-app",
  "verifyCommands": [["mvn", "test"]],
  "maxRepairRounds": 2
}
```

响应：

```json
{
  "success": true,
  "data": {
    "runId": "...",
    "dsl": {},
    "harnessPrompt": "ROLE\n...",
    "generation": {
      "successful": true,
      "status": "VERIFIED",
      "projectRoot": "..."
    },
    "createdAt": "..."
  }
}
```

## 查询交付运行证据

```http
GET /api/orchestrator/delivery-runs
Authorization: Bearer <token>
```

返回服务端持久化的交付运行记录列表。当前实现使用 JSON 文件存储，默认路径为 `runtime-logs/delivery-runs`。

```http
GET /api/orchestrator/delivery-runs/{runId}
Authorization: Bearer <token>
```

响应：

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
  "createdAt": "2026-05-30T10:00:00Z",
  "updatedAt": "2026-05-30T10:01:00Z"
}
```

字段说明：

| 字段 | 含义 |
| --- | --- |
| `runId` | 交付运行 ID。 |
| `workspaceId` | 客户或项目工作区 ID；当前默认为 `demo-workspace`。 |
| `playbookId` | 使用的交付 Playbook ID；当前默认为 `compile-generate-verify-repair`。 |
| `status` | 运行状态：`REQUESTED`、`RUNNING`、`COMPLETED`、`FAILED`、`CANCELLED`。 |
| `request` | 原始运行请求。 |
| `response` | 终态运行响应。 |
| `events` | 运行事件流，可用于还原执行时间线。 |

## 查询 Playbook 模板

```http
GET /api/graph/playbooks
Authorization: Bearer <token>
```

返回可复用交付 Playbook 模板。当前内置默认模板为 `compile-generate-verify-repair`，对应：

```text
compile -> generate -> verify -> repair
```

响应：

```json
{
  "success": true,
  "data": [
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
}
```

## 编译 Graph

```http
POST /api/graph/compile
Content-Type: application/json
Authorization: Bearer <token>
```

请求：

```json
{
  "id": "compile-generate-verify-repair",
  "name": "Compile Generate Verify Repair",
  "startNodeId": "start",
  "maxIterations": 3,
  "nodes": [
    { "id": "start", "label": "start", "type": "start", "config": {} },
    { "id": "end", "label": "end", "type": "end", "config": {} }
  ],
  "edges": [
    { "id": "start-end", "source": "start", "target": "end", "condition": "success" }
  ]
}
```

响应包含 `valid`、`errors`、`warnings` 与规范化后的 graph。

## 运行 Graph

```http
POST /api/graph/run
Content-Type: application/json
Authorization: Bearer <token>
```

如果请求体不传 `graph`，后端会使用默认 Playbook 图。

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
