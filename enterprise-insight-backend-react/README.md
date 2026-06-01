# FDE 交付工作台前端

该目录是 Enterprise Insight Platform 的 React 前端。当前产品定位为 FDE 交付工作台，前端负责项目理解、Playbook、交付运行、运行证据和系统设置等交互。

前端只负责配置、请求、状态展示和事件渲染，不承载后端执行逻辑。

## 技术栈

- React
- TypeScript
- Vite
- React Router
- Zustand
- React Flow / `@xyflow/react`
- Tailwind CSS
- Axios

## 功能模块

| 模块 | 说明 |
| --- | --- |
| `features/auth` | 登录、JWT 本地存储和路由保护。 |
| `features/dsl` | DSL 输入和 Prompt 编译入口。 |
| `features/graph` | Playbook 加载、Graph Builder、Graph Runtime 状态展示。 |
| `features/project` | 项目理解看板、交付机会和可运行交付简报。 |
| `features/run` | Delivery Run 表单、SSE 运行事件、执行时间线和结果展示。 |
| `features/history` | Run Evidence 列表和运行详情。 |
| `shared` | 布局、导航、错误边界、通知和通用组件。 |

## 本地开发

```powershell
npm install
npm run dev -- --host 127.0.0.1
```

默认访问：

```text
http://127.0.0.1:5173
```

默认代理：

```text
/api      -> http://localhost:8080
/actuator -> http://localhost:8080
```

## 构建与检查

```powershell
npm run build
npm run lint
```

## 页面说明

### Project Intel

路径：

```text
/project
```

用途：

- 调用 `/api/project-analysis/current` 获取当前项目清单。
- 展示模块、API、前端路由、文档、测试和业务能力候选。
- 调用 `/api/project-analysis/current/delivery-brief` 获取可运行交付简报。
- 点击“装载到交付运行”后跳转到 `/run`，并带入 requirement、目标目录、验证命令和修复轮次。

### Playbooks

路径：

```text
/graph
```

用途：

- 调用 `/api/graph/playbooks` 加载内置 Playbook。
- 将 Playbook Graph 装载到 Graph Builder。
- 支持编译 Graph、生成 Prompt、运行 Graph。

### Delivery Run

路径：

```text
/run
```

用途：

- 编辑或接收交付 requirement。
- 配置模型、目标目录、验证命令和修复轮次。
- 调用 `/api/orchestrator/run/start` 启动运行。
- 通过 `/api/orchestrator/run/stream/{runId}` 消费 SSE 事件。
- 展示 compile、generate、verify、repair 状态。

### Run Evidence

路径：

```text
/runs
```

用途：

- 调用 `/api/orchestrator/delivery-runs` 查询服务端持久化运行记录。
- 查看单次运行的输入、事件、输出和状态。

## 前端边界

前端禁止实现：

- Graph 后端执行。
- 模型调用。
- 文件写入。
- 验证命令执行。
- 自动修复。
- DeliveryRun 持久化。

这些能力由后端服务负责，前端通过 API 和 SSE 消费结果。
