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

## Run Harness Compiler Platform

```bash
cd backend
mvn -pl harness-compiler-platform -am spring-boot:run
```

## Local LLM Configuration

```yaml
llm:
  model-type: local
  base-url: http://localhost:11434
  model: llama3
```

## Remote LLM Configuration

```yaml
llm:
  model-type: remote
  api-key: ${OPENAI_API_KEY}
  base-url: https://api.openai.com/v1
  model: gpt-4o-mini
```
