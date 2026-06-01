# 开发指南

## 环境要求

- Java 21+，用于 Spring Boot 服务。
- Maven 3.9+，用于后端构建和测试。
- Node.js 20+ 和 npm，用于 React 前端。
- Docker Desktop 或 Docker Engine，按需启动本地依赖。
- Ollama，可选，用于本地模型执行。
- k6，可选，用于压测。

## 后端测试

运行全部后端测试：

```powershell
cd backend
mvn test
```

只验证 orchestrator 及其依赖模块：

```powershell
cd backend
mvn -q -pl orchestrator-service -am test
```

## 前端构建与检查

```powershell
cd enterprise-insight-backend-react
npm install
npm run build
npm run lint
```

启动前端开发服务：

```powershell
npm run dev -- --host 127.0.0.1
```

默认地址：

```text
http://127.0.0.1:5173
```

Vite 默认把 `/api` 和 `/actuator` 代理到 `http://localhost:8080`。

## 启动后端服务

安装 orchestrator 依赖模块：

```powershell
cd backend
mvn -pl orchestrator-service -am -DskipTests install
```

单独启动 orchestrator：

```powershell
cd backend/orchestrator-service
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8091
```

## 本地依赖服务

`backend/compose.yaml` 提供 Nacos、MySQL、Redis、Qdrant 和 Ollama 等本地依赖。

```powershell
docker compose -f backend/compose.yaml up -d
```

停止：

```powershell
docker compose -f backend/compose.yaml down
```

## 项目扫描配置

项目理解功能由 orchestrator 提供，配置项如下：

```yaml
project:
  analysis:
    root: ${EIP_PROJECT_ANALYSIS_ROOT:}
    max-depth: 8
    max-files: 5000
    max-evidence-per-category: 80
```

说明：

- `root` 为空时，服务会从当前工作目录向上查找包含 `backend` 和 `enterprise-insight-backend-react` 的仓库根目录。
- `max-depth` 控制扫描深度。
- `max-files` 控制扫描文件上限。
- `max-evidence-per-category` 控制每类证据返回数量。

## DeliveryRun 持久化配置

```yaml
delivery:
  run-store:
    storage-root: ${EIP_DELIVERY_RUN_STORAGE_ROOT:runtime-logs/delivery-runs}
    max-list-size: 100
```

当前第一阶段使用 JSON 文件持久化。后续会抽象 repository 并迁移到 H2/PostgreSQL。

## 模块边界

- `harness-compiler`：负责需求或 Graph 到 DSL/Prompt 的编译，不直接调用模型。
- `orchestrator-service`：负责项目理解、Playbook、运行编排、SSE 事件和 DeliveryRun 记录。
- `agent-adapter`：负责模型调用、项目文件写入、验证命令执行和自动修复。
- `gateway-service`：负责鉴权和路由。
- `enterprise-insight-backend-react`：负责 FDE 工作台前端交互和状态展示。

## 推荐开发顺序

1. 后端先补契约和单元测试。
2. 再补前端类型和 API 模块。
3. 再接页面交互。
4. 最后跑 Maven 测试、前端 build、前端 lint 和必要的浏览器验收。

## 常用验证清单

```powershell
cd backend
mvn -q -pl orchestrator-service -am test
```

```powershell
cd enterprise-insight-backend-react
npm run build
npm run lint
```

```powershell
git diff --check
```

## 压测

```powershell
k6 run scripts/k6/graph-runtime-load.js
```

直连 orchestrator 的本地压测模式：

```powershell
$env:BASE_URL="http://localhost:8091"
$env:AUTH_MODE="none"
$env:SKIP_COMPILER="true"
k6 run scripts/k6/graph-runtime-load.js
```
