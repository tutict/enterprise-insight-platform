# 后端帮助

后端工程是 Enterprise Insight Platform 的 Spring Boot 多模块服务集合，支撑 FDE 交付工作台的鉴权、编译、项目理解、运行编排、Agent 执行、验证修复和交付证据持久化。

## 模块说明

| 模块 | 职责 |
| --- | --- |
| `gateway-service` | JWT 鉴权、角色校验和 API 路由。 |
| `auth-service` | 登录和 token 签发。 |
| `metadata-service` | 元数据能力。 |
| `harness-compiler` | 需求或 Graph 到 DSL/Prompt 的编译。 |
| `orchestrator-service` | 项目理解、Playbook、DeliveryRun、SSE 事件和运行编排。 |
| `agent-adapter` | 模型调用、文件写入、验证命令执行和自动修复。 |
| `ai-service` | AI 相关扩展服务。 |
| `common` | 公共响应、错误码和安全工具。 |

## 构建与测试

运行全部测试：

```powershell
mvn test
```

只验证 orchestrator 及依赖模块：

```powershell
mvn -q -pl orchestrator-service -am test
```

安装 orchestrator 依赖模块：

```powershell
mvn -pl orchestrator-service -am -DskipTests install
```

## 启动 orchestrator

```powershell
cd orchestrator-service
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8091
```

## 本地依赖

`compose.yaml` 提供 Nacos、MySQL、Redis、Qdrant 和 Ollama 等本地依赖。

```powershell
docker compose up -d
```

停止：

```powershell
docker compose down
```

## FDE 相关后端能力

### 项目理解

位置：

```text
orchestrator-service/src/main/java/com/tutict/eip/orchestrator/project
```

核心类：

- `ProjectScannerService`
- `ProjectAnalysisController`
- `ProjectInventory`
- `ProjectDeliveryBrief`
- `ProjectAnalysisProperties`

接口：

- `GET /api/project-analysis/current`
- `GET /api/project-analysis/current/delivery-brief`

### Playbook

位置：

```text
orchestrator-service/src/main/java/com/tutict/eip/orchestrator/graph/runtime
```

核心类：

- `PlaybookTemplateService`
- `PlaybookTemplateController`
- `DefaultGraphDefinitions`

接口：

- `GET /api/graph/playbooks`

### DeliveryRun

位置：

```text
orchestrator-service/src/main/java/com/tutict/eip/orchestrator/delivery
```

核心类：

- `DeliveryRunStore`
- `DeliveryRunRecord`
- `DeliveryRunController`

接口：

- `GET /api/orchestrator/delivery-runs`
- `GET /api/orchestrator/delivery-runs/{runId}`

### 运行事件

位置：

```text
orchestrator-service/src/main/java/com/tutict/eip/orchestrator/runtime
```

核心类：

- `RunStreamController`
- `RunExecutionEngine`
- `RunEventStreamService`

接口：

- `POST /api/orchestrator/run/start`
- `GET /api/orchestrator/run/stream/{runId}`
- `POST /api/orchestrator/run/control`

## 配置提示

项目扫描：

```yaml
project:
  analysis:
    root: ${EIP_PROJECT_ANALYSIS_ROOT:}
    max-depth: 8
    max-files: 5000
    max-evidence-per-category: 80
```

交付运行持久化：

```yaml
delivery:
  run-store:
    storage-root: ${EIP_DELIVERY_RUN_STORAGE_ROOT:runtime-logs/delivery-runs}
    max-list-size: 100
```

## Native Image

如果需要探索 GraalVM Native Image，可使用 Maven native profile。该路径不是当前默认交付路径，建议先保证 JVM 模式测试通过。

```powershell
.\mvnw native:compile -Pnative
.\mvnw test -PnativeTest
```
