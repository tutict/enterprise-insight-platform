# Architecture / 架构设计

## English

### Design Philosophy

This project treats Prompt Engineering as Compilation. Instead of directly generating code from natural language, the system introduces a controlled intermediate representation and runtime:

1. DSL as the intermediate representation.
2. Prompt Compiler as the transformation layer.
3. Orchestrator and GraphExecutor as the runtime.
4. Agent Adapter as the generation and verification boundary.

### Layers

- Input Layer: user requirements and visual graph definitions.
- DSL Layer: structured requirement and workflow representation.
- Compiler Layer: DSL-to-Prompt and Graph-to-DSL compilation.
- Runtime Layer: graph execution, SSE events, orchestration, verification, and repair.
- Agent Layer: LLM provider integration and generated file writing.

### Runtime Flow

```text
User Requirement or Graph
  -> CompileController or GraphCompileController
  -> DSLParser or GraphToDslCompiler
  -> PromptCompiler
  -> OrchestratorService or GraphExecutor
  -> AgentAdapter / Event Stream
  -> Generated Project / Runtime Events
```

### Package Layout

```text
controller/  HTTP boundaries and request validation
service/     Application use cases and orchestration
graph/       Graph model, compile service, executor, and SSE stream
runtime/     Run event streaming and execution engine
experiment/  Deterministic AB assignment
domain/      Request, response, and DSL models
config/      Spring Boot configuration properties and beans
```

## 中文

### 设计理念

本项目将 Prompt Engineering 视为编译过程。系统不会直接从自然语言生成代码，而是引入可控的中间表示和运行时：

1. DSL 作为中间表示。
2. Prompt Compiler 作为转换层。
3. Orchestrator 与 GraphExecutor 作为运行时。
4. Agent Adapter 作为生成、验证与文件写入边界。

### 分层

- 输入层：用户需求与可视化 Graph Definition。
- DSL 层：结构化需求与工作流表示。
- 编译层：DSL-to-Prompt 与 Graph-to-DSL 编译。
- 运行时层：图执行、SSE 事件、编排、验证与修复。
- Agent 层：LLM Provider 集成与生成文件写入。

### 运行流程

```text
用户需求或 Graph
  -> CompileController 或 GraphCompileController
  -> DSLParser 或 GraphToDslCompiler
  -> PromptCompiler
  -> OrchestratorService 或 GraphExecutor
  -> AgentAdapter / Event Stream
  -> 生成项目 / 运行事件
```

### 包结构

```text
controller/  HTTP 边界与请求校验
service/     应用用例与编排
graph/       Graph 模型、编译服务、执行器与 SSE 事件流
runtime/     Run 事件流与执行引擎
experiment/  确定性 AB 实验分配
domain/      请求、响应与 DSL 模型
config/      Spring Boot 配置属性与 Bean
```
