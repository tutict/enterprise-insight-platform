# API / 接口文档

## 基础地址 / Base URL

通过 Gateway 访问 / Through gateway:

```text
http://localhost:8080
```

本地直连 orchestrator / Direct local orchestrator:

```text
http://localhost:8091
```

## 登录 / Login

```http
POST /api/auth/login
Content-Type: application/json
```

请求 / Request:

```json
{
  "username": "admin",
  "password": "admin"
}
```

响应 / Response:

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

## 编译需求 / Compile Requirement

```http
POST /api/compiler/compile
Content-Type: application/json
Authorization: Bearer <token>
```

请求 / Request:

```json
{
  "requirement": "Build a Spring Boot login system with user management and database persistence"
}
```

响应 / Response:

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

## 从 Graph 编译 Prompt / Compile Prompt From Graph

```http
POST /api/compiler/from-graph
Content-Type: application/json
Authorization: Bearer <token>
```

该接口将可视化 Graph Definition 转换为 DSL flow，再生成 Harness Prompt。

This endpoint converts a visual Graph Definition into a DSL flow and then compiles a Harness Prompt.

## 运行编排 / Run Orchestration

```http
POST /api/orchestrator/run
Content-Type: application/json
Authorization: Bearer <token>
```

请求 / Request:

```json
{
  "requirement": "Build a Spring Boot login system with user management and database persistence",
  "model": "llama3.1",
  "targetDirectory": "generated-harness-app",
  "verifyCommands": [["mvn", "test"]],
  "maxRepairRounds": 2
}
```

响应 / Response:

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

## 编译 Graph / Compile Graph

```http
POST /api/graph/compile
Content-Type: application/json
Authorization: Bearer <token>
```

请求 / Request:

```json
{
  "id": "visual-builder-graph",
  "name": "Visual Builder Graph",
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

The response includes `valid`, `errors`, `warnings`, and the normalized graph.

## 运行 Graph / Run Graph

```http
POST /api/graph/run
Content-Type: application/json
Authorization: Bearer <token>
```

响应 / Response:

```json
{
  "success": true,
  "data": {
    "runId": "...",
    "graph": {}
  }
}
```

## Graph SSE 事件流 / Graph SSE Stream

```http
GET /api/graph/run/stream/{runId}
Accept: text/event-stream
Authorization: Bearer <token>
```

事件类型 / Event types:

```text
GRAPH_RUN_STARTED
NODE_STARTED
NODE_SUCCEEDED
NODE_FAILED
EDGE_TRAVERSED
GRAPH_RUN_COMPLETED
GRAPH_RUN_FAILED
```
