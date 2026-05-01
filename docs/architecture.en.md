# Architecture

## Design Philosophy

This project treats **Prompt Engineering as Compilation**.

Instead of directly generating code from natural language, we introduce:

1. DSL (Intermediate Representation)
2. Prompt Compiler
3. Execution Layer (LLM)

## Layers

### 1. Input Layer

User natural language requirement.

### 2. DSL Layer

Structured representation of requirement.

### 3. Compiler Layer

Transforms DSL -> Prompt.

### 4. Execution Layer

Runs prompt using LLM.

## Key Idea

> Separate "what to build" from "how to generate"

## Runtime Flow

```text
User Requirement
  -> CompileController
  -> DSLParser
  -> DSLModel
  -> PromptCompiler
  -> Harness Prompt
  -> GenerateController
  -> CodeGenerationService
  -> AdapterFactory
  -> LLMAdapter
  -> Generated Code
```

## Package Layout

```text
controller/  HTTP boundary and request validation
service/     Application use cases and orchestration
compiler/    DSL to Harness Prompt compilation
agent/       LLM provider strategy implementations
domain/      Request, response, and DSL models
config/      Spring Boot configuration properties and beans
```
