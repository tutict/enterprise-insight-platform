# Backend Help / 后端帮助

## 简介 / Overview

后端工程是 Enterprise Insight Platform 的 Spring Boot 多模块服务集合，包含 gateway、auth、metadata、harness-compiler、orchestrator、agent-adapter、ai-service 与 common 模块。系统支持需求编译、Prompt 编译、Graph runtime、SSE 事件流、Agent 生成、验证命令执行和自动修复。

The backend project is a Spring Boot multi-module service set for Enterprise Insight Platform. It includes gateway, auth, metadata, harness-compiler, orchestrator, agent-adapter, ai-service, and common modules. It supports requirement compilation, prompt compilation, graph runtime, SSE event streams, agent generation, verification command execution, and auto-repair.

## 构建与测试 / Build and Test

```bash
mvn test
```

安装 orchestrator 依赖模块 / Install orchestrator dependencies:

```bash
mvn -pl orchestrator-service -am -DskipTests install
```

## 启动 orchestrator / Run Orchestrator

```bash
cd orchestrator-service
mvn spring-boot:run
```

默认端口 / Default port:

```text
http://localhost:8091
```

## Docker Compose

`compose.yaml` 提供 Nacos、MySQL、Redis、Qdrant 与 Ollama 的本地依赖服务。

`compose.yaml` provides local dependency services for Nacos, MySQL, Redis, Qdrant, and Ollama.

```bash
docker compose up -d
```

## Native Image / 原生镜像

如果需要探索 GraalVM Native Image，可使用 Maven native profile。该路径不是默认交付路径，建议先保证 JVM 模式测试通过。

If GraalVM Native Image is needed, use the Maven native profile. This is not the default delivery path; validate JVM-mode tests first.

```bash
./mvnw native:compile -Pnative
./mvnw test -PnativeTest
```

## 模块说明 / Module Notes

- `gateway-service`：JWT 鉴权与路由 / JWT authentication and routing.
- `harness-compiler`：DSL 与 Prompt 编译 / DSL and prompt compilation.
- `orchestrator-service`：业务编排、Graph runtime、SSE 事件 / orchestration, graph runtime, and SSE events.
- `agent-adapter`：LLM 调用、文件写入、验证与修复 / LLM calls, file writing, verification, and repair.
- `common`：公共响应、错误码与安全工具 / shared responses, error codes, and security utilities.
