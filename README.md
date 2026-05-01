# AI Harness Compiler Platform

> From Requirement -> DSL -> Prompt -> Code

## Overview

This project is an AI Harness Compiler Platform, designed to transform natural language requirements into structured DSL, compile them into engineering-grade prompts, and generate code using LLMs.

Unlike typical AI applications, this system focuses on:

- Prompt Engineering as Compilation
- Structured DSL for controllability
- Pluggable LLM runtime, local and remote

## Architecture

```text
User Input
   |
   v
DSL Parser
   |
   v
Prompt Compiler
   |
   v
LLM Adapter
   |
   v
Generated Code
```

## Core Components

### 1. DSL Parser

Transforms natural language into structured DSL.

### 2. Prompt Compiler

Compiles DSL into structured Harness Prompt.

### 3. LLM Adapter

Supports:

- Remote LLM, OpenAI
- Local LLM, Ollama

### 4. Code Generation Service

Executes prompt and returns generated code.

## API

### POST /compile

Generate DSL and Prompt.

### POST /generate

Generate code using LLM.

## Example

```json
{
  "requirement": "Build a user login system"
}
```

## Features

- DSL-driven code generation
- Pluggable LLM runtime
- Structured prompt generation
- Agent-based execution

## Tech Stack

- Java 17+
- Spring Boot 3
- LLM Integration, OpenAI and Ollama

## Run

```bash
./scripts/run.sh
```

Windows PowerShell:

```powershell
.\scripts\run-harness.ps1
```

## Inspiration

This project explores the idea of:

> AI as a Compiler for Software Engineering

## Future Work

- Multi-step agent loop
- Code auto-fix
- Visual DSL editor

## License

MIT
