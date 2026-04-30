# AI Harness Compiler

AI Harness Compiler is an engineering-oriented AI platform that converts software requirements into structured DSL, compiles the DSL into stable Harness Prompts, and drives local or remote Agents to generate, verify, and repair code.

The project focuses on engineering-grade AI code generation. It separates user intent, DSL structure, prompt compilation, local agent execution, verification, and automatic repair into explicit modules.

## Architecture

```text
User Requirement
      |
      v
React UI
      |
      v
Gateway / Auth
      |
      v
Harness Orchestrator
      |
      +--> DSL Parser
      |       Requirement / YAML -> DSLDocument
      |
      +--> Prompt Compiler
      |       DSLDocument + Template -> Harness Prompt
      |
      +--> Agent Adapter
              Harness Prompt -> Local LLM / Ollama
                    |
                    v
              Generated Project Files
                    |
                    v
              Compile / Test / Error Capture
                    |
                    v
              Agent Repair Loop
```

Backend module layout:

```text
enterprise-insight-backend/
|-- common
|-- gateway-service
|-- auth-service
|-- metadata-service
|-- prompt-compiler
|-- agent-adapter
|-- harness-compiler-platform
`-- ai-service
```

Frontend:

```text
enterprise-insight-backend-react/
`-- src/pages/HarnessCompiler.tsx
```

## Core Innovations

### DSL -> Prompt -> Code

The system introduces a deterministic generation pipeline:

```text
Requirement
  -> DSLDocument
  -> Harness Prompt
  -> Agent Execution
  -> Generated Code
```

Example DSL:

```yaml
project:
  type: spring_boot
  modules:
    - user
    - auth
    - leaderboard
constraints:
  db: mysql
```

Compiled Harness Prompt:

```text
# ROLE
You are an AI Harness coding agent that generates production-ready Java and Spring Boot code.

# GOAL
Convert the structured DSL into a complete, compilable implementation plan and code files.

# MODULES
- project_type: spring_boot
- module: user
- module: auth
- module: leaderboard

# CONSTRAINTS
- db: mysql

# OUTPUT FORMAT
Return only generated files using this exact format:
===FILE START===
relative/path/from/project/root
complete file content
===FILE END===
```

### Prompt Compiler

`prompt-compiler` is the core compiler module. It provides:

- YAML DSL parsing with Jackson
- Java domain model validation
- Stable Prompt generation
- Extensible template registry
- Deterministic output sections: `ROLE`, `GOAL`, `MODULES`, `CONSTRAINTS`, `OUTPUT FORMAT`

The compiler treats prompts as engineered artifacts rather than ad hoc strings.

### Agent Loop

`agent-adapter` implements an automated repair loop:

```text
Generate Code
  -> Write Project Files
  -> Run Verification Commands
  -> Capture stdout / stderr / exitCode
  -> Feed Error Back to LLM
  -> Regenerate Corrected Project
  -> Stop on Success or maxRepairRounds
```

Termination is explicit:

- Stop when verification passes
- Stop when `maxRepairRounds + 1` attempts are exhausted

This makes the system closer to an AI engineering harness than a one-shot chatbot.

## Tech Stack

Backend:

- Java 21
- Spring Boot 3
- Spring Cloud 2023
- Maven multi-module architecture
- Jackson YAML
- JDK `HttpClient`
- Ollama local LLM API
- SLF4J logging
- JUnit 5 / AssertJ

Frontend:

- React 19
- TypeScript
- Vite
- React Router

Reusable platform modules:

- `gateway-service`: API routing
- `auth-service`: authentication and JWT
- `common`: shared DTOs, error codes, security helpers
- `metadata-service`: platform metadata

## Usage

### Start Infrastructure

```bash
cd enterprise-insight-backend
docker compose up -d nacos mysql redis qdrant ollama
```

Pull a local model:

```bash
ollama pull llama3.1
```

### Run Prompt Compiler

```bash
cd enterprise-insight-backend
mvn -pl prompt-compiler -am spring-boot:run
```

Endpoint:

```http
POST /api/prompt-compiler/compile
```

Request:

```json
{
  "dsl": "project:\n  type: spring_boot\n  modules:\n    - user\n    - auth\n    - leaderboard\nconstraints:\n  db: mysql",
  "templateName": "harness-default"
}
```

### Run Agent Adapter

```bash
cd enterprise-insight-backend
mvn -pl agent-adapter -am spring-boot:run
```

Default config:

```yaml
agent:
  ollama:
    base-url: http://localhost:11434
    model: llama3.1
    max-retries: 2
    retry-backoff: 1s
    verification-timeout: 3m
    output-root: ./agent-output
```

Auto-repair endpoint:

```http
POST /api/agent-adapter/auto-repair/generate
```

Request:

```json
{
  "model": "llama3.1",
  "targetDirectory": "demo-harness-app",
  "maxRepairRounds": 2,
  "verifyCommands": [["mvn", "test"]],
  "prompt": "Generate a Spring Boot 3 Maven project with user, auth, and leaderboard modules."
}
```

Generated files are written under:

```text
enterprise-insight-backend/agent-output/demo-harness-app/
```

### Run React UI

```bash
cd enterprise-insight-backend-react
npm install
npm run dev
```

Open:

```text
http://localhost:5173/harness
```

The UI supports:

- Requirement input
- DSL preview
- Harness Prompt preview
- Code-generation trigger
- Generation status display

## Example Flow

Input requirement:

```text
Build a Spring Boot application with user, auth, and leaderboard modules using MySQL.
```

Generated DSL:

```yaml
project:
  type: spring_boot
  modules:
    - user
    - auth
    - leaderboard
constraints:
  db: mysql
```

Agent output protocol:

```text
===FILE START===
pom.xml
complete file content
===FILE END===
===FILE START===
src/main/java/com/example/Application.java
complete file content
===FILE END===
```

Verification:

```bash
mvn test
```

If verification fails, stderr/stdout are appended to a repair prompt and sent back to the LLM.

## Why This Project Matters

This project demonstrates engineering-grade AI system design:

- It separates intent, structure, prompt compilation, and execution.
- It treats prompts as compiler output, not UI text.
- It uses local LLM deployment through Ollama.
- It includes retries, logging, validation, file-system safety, and termination conditions.
- It moves from one-shot generation to an Agent repair loop.

For interviews, the key architectural point is that this is not a wrapper around an LLM API. It is a compiler-inspired AI harness that turns unstructured requirements into deterministic prompts and verifiable code-generation workflows.

## Verification Commands

Backend tests:

```bash
cd enterprise-insight-backend
mvn test
```

Frontend build:

```bash
cd enterprise-insight-backend-react
npm run build
```

## Roadmap

- Persist DSL, Prompt, and Agent runs in `metadata-service`
- Add multi-template Prompt compilation
- Add project-level diff and patch application
- Add streaming repair progress in the React UI
- Add provider adapters for OpenAI-compatible APIs, Qwen, DeepSeek, and vLLM
- Add sandboxed verification runners for safer command execution
