# Development / 开发指南

## 环境要求 / Prerequisites

- Java 21+，用于 Spring Boot 服务 / Java 21+ for Spring Boot services.
- Maven 3.9+，用于后端构建与测试 / Maven 3.9+ for backend build and tests.
- Node.js 20+ 与 npm，用于 React 控制台 / Node.js 20+ and npm for the React console.
- k6，用于压测 / k6 for load testing.
- Ollama，可选，用于本地 LLM 执行 / Ollama is optional for local LLM execution.

## 后端测试 / Backend Tests

```bash
cd backend
mvn test
```

该命令会运行 compiler、orchestrator、agent-adapter、gateway、graph runtime、security 与 API contract 测试。

This command runs compiler, orchestrator, agent-adapter, gateway, graph runtime, security, and API contract tests.

## 前端构建 / Frontend Build

```bash
cd enterprise-insight-backend-react
npm install
npm run build
```

Windows 沙箱环境可能会阻止 Vite/esbuild 子进程，需要在普通终端或授权环境中执行。

Windows sandbox environments may block Vite/esbuild child processes; run the command in a regular or approved shell if needed.

## 启动服务 / Run Services

安装本地依赖模块 / Install local backend modules:

```bash
cd backend
mvn -pl orchestrator-service -am -DskipTests install
```

单独启动 orchestrator / Run orchestrator only:

```bash
cd backend/orchestrator-service
mvn spring-boot:run
```

默认端口 / Default port:

```text
http://localhost:8091
```

## 本地 Agent 配置 / Local Agent Configuration

```yaml
agent:
  ollama:
    base-url: http://localhost:11434
    model: llama3.1
    verification-timeout: 3m
    output-root: ./agent-output
```

## 模块边界 / Module Boundaries

- `harness-compiler`：只负责将需求或 Graph Definition 编译成 DSL 与 Prompt，不直接调用 LLM。
- `orchestrator-service`：负责串联 compiler、agent-adapter、GraphExecutor 与 SSE 事件流。
- `agent-adapter`：负责模型调用、项目文件写入、验证命令执行与自动修复循环。
- `enterprise-insight-backend-react`：负责可视化配置和事件渲染，不包含后端执行逻辑。

- `harness-compiler`: compiles requirements or graph definitions into DSL and prompts; it does not call LLMs directly.
- `orchestrator-service`: coordinates compiler output, agent-adapter execution, GraphExecutor, and SSE event streams.
- `agent-adapter`: owns model calls, project file writing, verification command execution, and auto-repair loops.
- `enterprise-insight-backend-react`: owns visual configuration and event rendering; it does not contain backend execution logic.

## 压测 / Load Testing

```bash
k6 run scripts/k6/graph-runtime-load.js
```

直连 orchestrator 的本地模式 / Local direct orchestrator mode:

```powershell
$env:BASE_URL="http://localhost:8091"
$env:AUTH_MODE="none"
$env:SKIP_COMPILER="true"
k6 run scripts/k6/graph-runtime-load.js
```
