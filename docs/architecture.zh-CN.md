# 架构设计

## 设计理念

本项目将 **Prompt Engineering 视为一种编译过程**。

系统不会直接从自然语言生成代码，而是引入三个明确阶段：

1. DSL（中间表示）
2. Prompt Compiler（提示词编译器）
3. Execution Layer（LLM 执行层）

## 分层设计

### 1. 输入层

接收用户的自然语言需求。

### 2. DSL 层

将需求转换为结构化描述，作为后续编译和生成的中间表示。

### 3. 编译层

将 DSL 转换为结构化 Harness Prompt。

### 4. 执行层

通过 LLM 执行 Prompt，并返回生成代码。

## 核心思想

> 分离“要构建什么”和“如何生成”

## 运行流程

```text
用户需求
  -> CompileController
  -> DSLParser
  -> DSLModel
  -> PromptCompiler
  -> Harness Prompt
  -> GenerateController
  -> CodeGenerationService
  -> AdapterFactory
  -> LLMAdapter
  -> 生成代码
```

## 包结构

```text
controller/  HTTP 接口边界与请求校验
service/     应用用例编排
compiler/    DSL 到 Harness Prompt 的编译逻辑
agent/       LLM Provider 策略实现
domain/      请求、响应与 DSL 领域模型
config/      Spring Boot 配置属性与 Bean 定义
```
