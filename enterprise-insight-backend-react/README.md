# Enterprise Insight React Console / 企业洞察前端控制台

## 简介 / Overview

该前端工程是 Enterprise Insight Platform 的 React 控制台，负责 DSL 编辑、Graph Builder 可视化编排、运行态事件展示、历史运行记录和系统设置。前端只负责配置、请求和事件渲染，不承载 Graph 执行逻辑。

This frontend project is the React console for Enterprise Insight Platform. It provides DSL editing, visual Graph Builder orchestration, runtime event rendering, run history, and settings. The frontend owns configuration, requests, and event rendering, but it does not execute graph logic.

## 技术栈 / Tech Stack

- React 18
- TypeScript
- Vite
- React Router
- Zustand
- React Flow / `@xyflow/react`
- Tailwind CSS
- Axios

## 功能模块 / Feature Modules

- `features/dsl`：DSL 编辑与 Prompt 生成入口 / DSL editing and prompt generation entry.
- `features/graph`：Graph Builder、Config Panel、Graph Runtime 事件渲染 / Graph Builder, Config Panel, and graph runtime event rendering.
- `features/run`：运行控制台、SSE runtime adapter、执行时间线 / run console, SSE runtime adapter, and execution timeline.
- `features/history`：运行历史与详情 / run history and details.
- `features/auth`：登录、JWT 存储与路由保护 / login, JWT storage, and route protection.
- `shared`：布局、导航、状态、错误边界等通用组件 / shared layout, navigation, status, and error-boundary components.

## 本地开发 / Local Development

```bash
npm install
npm run dev -- --host 127.0.0.1
```

默认情况下，Vite 会把 `/api` 和 `/actuator` 代理到 `http://localhost:8080`。

By default, Vite proxies `/api` and `/actuator` to `http://localhost:8080`.

## 构建 / Build

```bash
npm run build
```

Windows 沙箱环境可能阻止 Vite/esbuild 子进程，必要时请在普通终端或授权环境中执行。

Windows sandbox environments may block Vite/esbuild child processes. Run in a regular or approved shell if needed.

## 运行约束 / Runtime Boundary

- UI 不能写后端执行逻辑 / The UI must not implement backend execution logic.
- Graph 执行由 `orchestrator-service` 的 `GraphExecutor` 负责 / Graph execution is owned by `GraphExecutor` in `orchestrator-service`.
- UI 通过 SSE 消费 `GraphEvent` 并更新状态 / The UI consumes `GraphEvent` through SSE and updates state.
- Graph 定义通过 `/api/graph/compile` 校验，通过 `/api/graph/run` 启动 / Graph definitions are validated through `/api/graph/compile` and started through `/api/graph/run`.
