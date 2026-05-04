# 架构设计 / Architecture

## 中文

### 设计理念

本项目将 Prompt Engineering 视为一种编译过程。系统不会直接从自然语言需求生成代码，而是通过 DSL、Prompt Compiler、Graph Runtime 与 Agent Adapter 建立可验证、可观测、可扩展的 AI 工程链路。

### 分层设计

1. 输入层：接收自然语言需求或可视化 Graph Definition。
2. DSL 层：将需求转换为结构化中间表示。
3. 编译层：将 DSL 或 Graph 编译为 Harness Prompt。
4. 运行时层：执行 Graph、推送 SSE 事件、协调条件分支与循环。
5. Agent 层：调用 LLM、写入生成文件、执行验证命令并触发自动修复。

### 核心思想

> 分离“要构建什么”和“如何生成”。

### 运行流程

```text
用户需求 / Graph
  -> CompileController / GraphCompileController
  -> DSLParser / GraphToDslCompiler
  -> PromptCompiler
  -> OrchestratorService / GraphExecutor
  -> AgentAdapter / GraphEventStream
  -> 生成项目 / 运行事件
```

### 包结构

```text
controller/  HTTP 接口边界与请求校验
service/     应用用例与业务编排
graph/       Graph 模型、校验、执行与事件流
runtime/     Run 执行引擎与 SSE 推送
experiment/  AB 实验分配
domain/      请求、响应与 DSL 领域模型
config/      Spring Boot 配置属性与 Bean 定义
```

## English

### Design Philosophy

This project treats Prompt Engineering as a compilation process. Instead of generating code directly from natural-language requirements, it introduces DSL, Prompt Compiler, Graph Runtime, and Agent Adapter to build a verifiable, observable, and extensible AI engineering pipeline.

### Layers

1. Input Layer: receives natural-language requirements or visual Graph Definitions.
2. DSL Layer: converts requirements into structured intermediate representations.
3. Compiler Layer: compiles DSL or Graph definitions into Harness Prompts.
4. Runtime Layer: executes graphs, streams SSE events, and coordinates conditional branches and bounded loops.
5. Agent Layer: calls LLMs, writes generated files, runs verification commands, and triggers auto-repair.

### Key Idea

> Separate what to build from how to generate.

### Runtime Flow

```text
User Requirement / Graph
  -> CompileController / GraphCompileController
  -> DSLParser / GraphToDslCompiler
  -> PromptCompiler
  -> OrchestratorService / GraphExecutor
  -> AgentAdapter / GraphEventStream
  -> Generated Project / Runtime Events
```

### Package Layout

```text
controller/  HTTP boundary and request validation
service/     Application use cases and orchestration
graph/       Graph model, validation, execution, and event stream
runtime/     Run execution engine and SSE streaming
experiment/  AB experiment assignment
domain/      Request, response, and DSL domain models
config/      Spring Boot configuration properties and bean definitions
```
