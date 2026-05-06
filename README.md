# Enterprise Insight Platform / 企业洞察平台

## 项目简介 / Project Introduction

将 AI Workflow Builder 与 Agent 执行能力引入 Enterprise Insight Platform，并围绕需求编译、可视化编排、图运行时与自动修复链路重新完善工程基线，补齐前端控制台、后端编译与编排服务、SSE 事件流、测试体系、压测脚本、安全策略与运维文档，提升项目完整性、可观测性与交付能力。项目基于 Spring Boot、React、React Flow、Zustand、k6、Ollama、Qdrant 与 Docker Compose 实现，支持 DSL 到 Prompt 编译、Graph Builder 可视化编排、条件分支与循环执行、运行时事件推送、容器化部署与生产环境扩展。

Enterprise Insight Platform brings AI Workflow Builder and Agent execution capabilities into a deployable engineering baseline. It covers requirement compilation, visual graph orchestration, graph runtime execution, and auto-repair workflows, while completing the frontend console, backend compiler/orchestrator services, SSE event streaming, tests, load scripts, security controls, and operation documentation. The platform is built with Spring Boot, React, React Flow, Zustand, k6, Ollama, Qdrant, and Docker Compose. It supports DSL-to-prompt compilation, visual graph building, conditional branches, bounded loops, runtime event streaming, containerized deployment, and production-oriented extension.

## 项目定位 / Positioning

本项目不是普通聊天应用，而是面向 AI 辅助交付的工程控制台。系统将自然语言需求转换为结构化 DSL 和 Harness Prompt，通过 Agent Adapter 生成项目文件，并使用验证命令与自动修复循环提高生成结果的可控性。

This project is not a generic chat application. It is an engineering control plane for AI-assisted delivery. It transforms natural-language requirements into structured DSL and Harness Prompts, generates project files through an Agent Adapter, and improves controllability through verification commands and auto-repair loops.

核心链路 / Core flow:

```text
Requirement / 需求
  -> DSL Compiler / DSL 编译
  -> Prompt Compiler / Prompt 编译
  -> Orchestrator / 编排服务
  -> Agent Adapter / Agent 适配器
  -> Generated Project / 生成项目
  -> Verification and Repair / 验证与修复
```

## 架构图 / Architecture

```text
React Console / 前端控制台
  |-- DSL Editor / DSL 编辑器
  |-- Graph Builder / 可视化图编排
  |-- Run Console / 运行控制台
  |
  v
Gateway Service :8080 / 网关服务
  |-- JWT Auth / JWT 鉴权
  |-- Role Routing / 角色路由
  |
  +--> Auth Service / 认证服务
  +--> Metadata Service / 元数据服务
  +--> Harness Compiler / 编译服务
  |      |-- RuleBasedDslParser
  |      |-- GraphToDslCompiler
  |      |-- PromptCompiler
  |      '-- Prompt Injection Guard / Prompt 注入防护
  |
  +--> Orchestrator Service :8091 / 编排服务
  |      |-- Pipeline Runtime / 流水线运行时
  |      |-- Graph Compile API / 图编译接口
  |      |-- GraphExecutor / 图执行器
  |      |-- SSE GraphEvent Stream / SSE 事件流
  |      '-- Experiment Assignment / AB 实验分配
  |
  '--> Agent Adapter / Agent 适配器
         |-- Ollama Adapter / Ollama 适配
         |-- File Writers / 文件写入器
         |-- verifyCommand Allowlist / 验证命令白名单
         '-- Auto Repair Loop / 自动修复循环
```

## 核心能力 / Core Capabilities

- Debug / 调试：Graph runtime 推送 `GRAPH_RUN_STARTED`、`NODE_STARTED`、`NODE_SUCCEEDED`、`NODE_FAILED`、`EDGE_TRAVERSED`、`GRAPH_RUN_COMPLETED` 和 `GRAPH_RUN_FAILED` 事件，前端只消费事件并渲染状态，不承载执行逻辑。
- Profiling / 性能分析：k6 脚本输出 latency、RPS 和 error rate；运行事件可进一步扩展为节点级耗时分析。
- AB Test / 实验能力：`ExperimentAssignmentService` 基于 `experimentKey` 与 `subjectKey` 做确定性加权分配，可用于 Prompt 模板、模型路由或修复策略实验。
- Security / 安全：Gateway 统一鉴权，Prompt 输入做隔离与清洗，`verifyCommand` 使用白名单，生成文件只能写入受控目录。

- Debug: the graph runtime emits lifecycle events, and the frontend renders state from events without owning execution logic.
- Profiling: k6 reports latency, RPS, and error rate; runtime events can be extended into node-level duration metrics.
- AB Test: `ExperimentAssignmentService` provides deterministic weighted assignment for prompt templates, model routing, or repair policies.
- Security: gateway authentication, prompt input isolation, verify command allowlists, and file-write sandboxing are implemented.

## 服务与接口 / Services and APIs

| 服务 / Service | 责任 / Responsibility | 核心接口 / Key APIs |
| --- | --- | --- |
| `gateway-service` | JWT 鉴权、角色校验、服务路由 / JWT auth, role checks, routing | `/api/**` |
| `auth-service` | 登录与 Token 签发 / Login and token issuing | `POST /api/auth/login` |
| `harness-compiler` | 需求或图编译为 DSL 与 Prompt / Compile requirements or graphs into DSL and prompts | `POST /api/compiler/compile`, `POST /api/compiler/from-graph` |
| `orchestrator-service` | 编排执行、Graph runtime、SSE 事件 / Orchestration, graph runtime, SSE events | `POST /api/orchestrator/run`, `POST /api/graph/compile`, `POST /api/graph/run`, `GET /api/graph/run/stream/{runId}` |
| `agent-adapter` | LLM 调用、文件写入、验证与修复 / LLM calls, file writing, verification, repair | `POST /api/agent-adapter/**` |

## 快速启动 / Quick Start

环境要求 / Prerequisites:

- Java 21+
- Maven 3.9+
- Node.js 20+
- npm
- k6, for load testing / 用于压测
- Ollama, optional for local LLM execution / 可选，用于本地模型执行

后端测试 / Backend tests:

```powershell
cd backend
mvn test
```

前端启动 / Frontend dev server:

```powershell
cd enterprise-insight-backend-react
npm install
npm run dev -- --host 127.0.0.1
```

登录 / Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

## 测试体系 / Test Strategy

当前测试覆盖 / Current coverage:

- Unit tests / 单元测试：compiler、orchestrator、agent-adapter、experiment assignment。
- Integration tests / 集成测试：`PipelineIntegrationTest` 覆盖 requirement -> DSL -> Prompt -> Orchestrator -> Agent Adapter -> File Writer -> Verifier。
- API contract tests / API 契约测试：compiler、orchestrator、graph runtime 接口响应结构。
- Security tests / 安全测试：JWT 鉴权、Prompt injection 防护、verifyCommand 限制、文件写入路径限制。

最新本地验证 / Latest local verification:

```text
mvn test                         PASS
npm run build                    PASS
k6 version                       PASS - k6.exe v1.7.1
```

## 性能结果 / Performance Results

k6 脚本 / k6 script:

```text
scripts/k6/graph-runtime-load.js
```

Gateway 模式 / Gateway mode:

```powershell
$env:BASE_URL="http://localhost:8080"
k6 run scripts/k6/graph-runtime-load.js
```

直连 orchestrator 模式 / Direct orchestrator mode:

```powershell
$env:BASE_URL="http://localhost:8091"
$env:AUTH_MODE="none"
$env:SKIP_COMPILER="true"
$env:VUS="10"
$env:RAMP_UP="30s"
$env:STEADY="1m"
$env:RAMP_DOWN="20s"
k6 run scripts/k6/graph-runtime-load.js
```

本地基线结果 / Local baseline result:

```text
date                             2026-05-04
scope                            graph runtime direct mode
baseUrl                          http://localhost:8091
authMode                         none
skipCompiler                     true
vus                              10
rampUp                           30s
steady                           1m
rampDown                         20s
requests                         1726
rps                              15.66
errorRate                        0
latency.avgMs                    0.86
latency.p95Ms                    1.90
latency.p99Ms                    3.18
thresholds                       PASS
```

## 安全策略 / Security Policy

- 受保护接口必须通过 Gateway JWT 鉴权 / Protected APIs require Gateway JWT authentication.
- `ANALYST` 角色可访问 compiler、graph、orchestrator、agent、metadata 与 AI 路由 / The `ANALYST` role is required for compiler, graph, orchestrator, agent, metadata, and AI routes.
- Prompt 输入被视为不可信数据，并进行注入语义清洗 / Prompt inputs are treated as untrusted data and sanitized.
- `verifyCommand` 仅允许 Maven、Gradle、npm、pnpm、Yarn 等构建测试命令 / `verifyCommand` is allowlisted to build and test executables such as Maven, Gradle, npm, pnpm, and Yarn.
- 生成文件路径必须保持在 `agent.ollama.output-root` 下 / Generated file paths must stay under `agent.ollama.output-root`.

详细说明见 / See also: [docs/security-testing.md](docs/security-testing.md).

## Graph Runtime 约束 / Graph Runtime Contract

- 必须且只能有一个 `start` 节点 / Exactly one `start` node is required.
- 至少需要一个 `end` 节点 / At least one `end` node is required.
- `condition` 节点必须有 success 与 failed/failure 分支 / Condition nodes must define success and failed/failure branches.
- 移除带 `maxIterations` 的循环边后，图必须是 DAG / The graph must be a DAG after bounded loop edges are removed.
- 循环边必须设置 `maxIterations` / Loop edges must define `maxIterations`.

## 路线图 / Roadmap

- 持久化 graph definition 与 run event log / Persist graph definitions and run event logs.
- 增加节点级耗时指标与 profiling 导出 / Add node-level timing metrics and profiling export.
- 将实验分配接入 orchestrator request metadata / Promote experiment assignment into orchestrator request metadata.
- 增加 OpenAPI 生成与 schema diff contract checks / Add OpenAPI generation and schema-diff contract checks.
- 发布 CI 性能基线 / Publish CI performance baselines.
- 增强租户级 verifyCommand policy / Add tenant-level verifyCommand policy profiles.

## Quick Start

Prerequisites:

- Docker Desktop or Docker Engine with Docker Compose
- Java 21+
- Maven 3.9+
- Node.js 20+
- npm

```bash
git clone <repo-url>
cd enterprise-insight-platform
./scripts/dev.sh
```

Windows PowerShell:

```powershell
.\scripts\dev.ps1
```

The dev script starts Docker base services, backend Spring Boot services, and the React Vite dev server. Default addresses:

```text
Frontend             http://127.0.0.1:5173
Gateway API          http://localhost:8080
Orchestrator API     http://localhost:8091
Qdrant               http://localhost:6333
Ollama               http://localhost:11434
```

Login with `admin` / `admin`. Runtime logs are written to `runtime-logs/`.

Stop Docker services when finished:

```bash
docker compose -f backend/compose.yaml down
```

## License / License
MIT
