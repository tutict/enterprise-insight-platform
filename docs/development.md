# Development

## Prerequisites

- Java 17 or newer
- Maven 3.9+
- Ollama for local model execution

## Run Tests

```bash
cd backend
mvn test
```

## Run Orchestrator Service

```bash
cd backend
mvn -pl orchestrator-service -am spring-boot:run
```

## Local Agent Configuration

```yaml
agent:
  ollama:
    base-url: http://localhost:11434
    model: llama3.1
```

## Compiler Boundary

`harness-compiler` only compiles requirement DSL into a structured prompt. It does not call LLMs.

## Orchestrator Boundary

`orchestrator-service` coordinates compiler output with `agent-adapter` execution and verification.
