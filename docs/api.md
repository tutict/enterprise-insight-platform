# API

Base URL through the gateway:

```text
http://localhost:8080
```

## Compile Requirement

```http
POST /api/compiler/compile
Content-Type: application/json
```

Request:

```json
{
  "requirement": "Build a Spring Boot login system with user management and database persistence"
}
```

Response:

```json
{
  "dsl": {
    "name": "ai-harness-generated-system",
    "type": "spring-boot-backend",
    "requirement": "Build a Spring Boot login system with user management and database persistence",
    "modules": ["api", "service", "domain", "authentication", "persistence"],
    "constraints": {
      "language": "Java 17+",
      "framework": "Spring Boot 3"
    },
    "outputFormat": "Return complete source files with paths and code blocks"
  },
  "prompt": "ROLE\n..."
}
```

## Run Orchestration

```http
POST /api/orchestrator/run
Content-Type: application/json
```

Request:

```json
{
  "requirement": "Build a Spring Boot login system with user management and database persistence",
  "model": "llama3.1",
  "targetDirectory": "generated-harness-app",
  "verifyCommands": [["mvn", "test"]],
  "maxRepairRounds": 2
}
```

Response:

```json
{
  "runId": "...",
  "dsl": {},
  "harnessPrompt": "ROLE\n...",
  "generation": {
    "successful": true,
    "status": "VERIFIED",
    "projectRoot": "..."
  },
  "createdAt": "..."
}
```
