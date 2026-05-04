# Architecture / 架构

## 文档入口 / Document Index

- English architecture details / 英文架构说明: [architecture.en.md](./architecture.en.md)
- 中文架构说明 / Chinese architecture details: [architecture.zh-CN.md](./architecture.zh-CN.md)

## 摘要 / Summary

系统采用编译器式 AI 工程架构：先将需求或可视化图转换为 DSL，再编译为 Harness Prompt，最后由 orchestrator 和 agent-adapter 执行生成、验证与修复。

The system uses a compiler-style AI engineering architecture: it converts requirements or visual graphs into DSL, compiles them into Harness Prompts, and then lets the orchestrator and agent-adapter execute generation, verification, and repair.

```text
Requirement / 需求
  -> DSL / 中间表示
  -> Prompt / 提示词
  -> Runtime / 运行时
  -> Agent / 生成与修复
```

## 核心原则 / Core Principles

- 分离“要构建什么”和“如何生成” / Separate what to build from how to generate.
- UI 只负责配置和事件渲染，不负责执行逻辑 / The UI owns configuration and event rendering, not execution logic.
- 后端运行时通过事件驱动暴露状态 / Backend runtimes expose state through events.
- 安全边界在 Prompt、命令执行和文件写入处同时生效 / Security boundaries are enforced at prompt, command execution, and file writing layers.
