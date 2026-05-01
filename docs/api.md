# API

Base URL for the MVP harness service:

```text
http://localhost:8090
```

## Compile Requirement

```http
POST /compile
Content-Type: application/json
```

Request:

```json
{
  "requirement": "做一个带登录的系统"
}
```

Response:

```json
{
  "dsl": {
    "name": "ai-harness-generated-system",
    "type": "spring-boot-backend",
    "requirement": "做一个带登录的系统",
    "modules": ["api", "service", "domain", "authentication"],
    "constraints": {
      "language": "Java 17+",
      "framework": "Spring Boot 3"
    },
    "outputFormat": "Return complete source files with paths and code blocks"
  },
  "prompt": "ROLE\n..."
}
```

## Generate Code

```http
POST /generate
Content-Type: application/json
```

Request:

```json
{
  "prompt": "ROLE\nYou are a senior Java architect..."
}
```

Response:

```json
{
  "code": "..."
}
```
