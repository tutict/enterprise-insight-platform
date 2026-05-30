# FDE 交付工作台路线图

## 定位

本项目正在从“AI 编排控制台”收束为 Forward Deployed Engineer（FDE）的交付工作台。

核心目标是把客户需求、现场上下文、实施方案、Agent 执行、验证结果和交付证据串成一条可复现、可审计、可复用的工程链路。

它不应该只是聊天界面或代码生成界面，而是 FDE 在客户现场做方案落地时的控制面板。

## 目标用户

- FDE / 解决方案工程师：把客户需求快速转成可运行原型、集成方案或修复补丁。
- 平台工程师：维护可复用的交付 Playbook、验证命令、模型路由和安全策略。
- 交付负责人：查看每次交付运行的输入、输出、验证状态、失败原因和修复记录。

## 核心工作流

```text
客户需求 / 现场上下文
  -> 需求结构化
  -> 交付 Playbook 编排
  -> DSL / Harness Prompt 编译
  -> Agent 执行
  -> 文件生成
  -> 验证命令
  -> 自动修复
  -> 交付证据与复盘
```

## 当前已落地

| 能力 | 实现位置 | 说明 |
| --- | --- | --- |
| FDE 产品文案 | React 控制台 i18n | 前端品牌、导航和运行页已收束为 FDE Delivery Workbench、Delivery Run、Run Evidence。 |
| 默认交付 Playbook | `orchestrator-service` 的 `PlaybookTemplateService` | 已将 `compile -> generate -> verify -> repair` 包装为 `compile-generate-verify-repair` 模板。 |
| Playbook 查询接口 | `GET /api/graph/playbooks` | 前端 Playbooks 页面可加载默认模板并写入 Graph Builder。 |
| 交付运行持久化 | `DeliveryRunStore` | 运行请求、事件流、最终响应和状态会写入 `runtime-logs/delivery-runs/*.json`。 |
| 交付证据查询接口 | `GET /api/orchestrator/delivery-runs` | Run Evidence 页面从服务端读取运行记录，不再依赖浏览器 localStorage 保存运行历史。 |
| 契约测试 | `DeliveryRunStoreTest`、`GraphApiContractTest` | 覆盖交付运行持久化和默认 Playbook API。 |

## 当前能力映射

| FDE 能力 | 当前模块 | 评价 |
| --- | --- | --- |
| 需求结构化 | `harness-compiler` | 已有规则型 DSL 解析，后续需要扩展客户上下文、约束和验收标准。 |
| 方案编排 | `Graph Builder` / `GraphExecutor` | 已有可视化图与后端运行时，适合沉淀交付 Playbook。 |
| Agent 执行 | `agent-adapter` | 已有 Ollama 适配、文件写入、验证与修复循环，后续需要扩展多 Provider。 |
| 运行可观测 | SSE 事件流 / 时间线 | 已有事件流，后续应补节点耗时、日志摘要和产物 diff。 |
| 安全边界 | Gateway / verify allowlist / output-root | 方向正确，后续需要租户级策略与审计记录。 |
| 交付资产 | `DeliveryRun` JSON 记录 | 已从前端 localStorage 下沉到服务端文件持久化，后续可替换为数据库。 |

## 产品主线

### 1. 客户工作区

面向客户或项目建立工作区，保存：

- 客户背景、系统栈、代码仓库、环境约束。
- 常用集成目标，例如 SSO、数据同步、API 网关、报表、权限模型。
- 验收标准、测试命令、部署约束。
- 交付运行历史和最终产物。

### 2. 交付 Playbook

把 Graph 从“临时流程图”升级为可复用 Playbook：

- discovery：澄清需求和缺失信息。
- plan：生成实施方案与风险点。
- implement：生成或修改项目文件。
- verify：运行测试、构建、安全检查。
- repair：基于失败输出自动修复。
- handoff：生成交付说明、变更摘要、后续事项。

### 3. 证据化运行

每次运行都要留下证据：

- 输入需求、编译后的 DSL、最终 Prompt。
- 模型、参数、验证命令、工作目录。
- 事件流、节点耗时、失败原因、修复轮次。
- 生成文件列表、diff、验证日志。
- 最终状态：已接受、待人工审阅、失败。

### 4. Provider 与策略层

FDE 工具必须能适配客户环境：

- Provider：Ollama、OpenAI-compatible API、企业内部模型网关。
- Policy：允许写入路径、允许执行命令、最大修复轮次、敏感信息过滤。
- Routing：按客户、任务类型、成本、延迟和安全要求选择模型。

## 里程碑

### Milestone 1：交付工作台壳

状态：已部分完成。

- 已将产品文案从 “AI Orchestrator” 收束为 “FDE Delivery Workbench”。
- 已新增 Playbooks、Delivery Run、Run Evidence 这组前端概念。
- Run Evidence 已从服务端读取交付运行记录。
- 仍需补一个真正的客户工作区入口。

验收标准：

- 新用户能在 5 分钟内理解这个项目服务于 FDE 交付。
- 默认页面能直接进入一个可运行的交付样例。

### Milestone 2：服务端持久化运行记录

状态：第一阶段已完成。

- 已持久化运行请求、运行事件、最终响应和运行状态。
- 已提供列表和详情接口。
- 已让前端 Run Evidence 页面从服务端查询。
- 后续需要补 generated file manifest、verification log 的独立索引，并迁移到 H2/PostgreSQL。

验收标准：

- 刷新浏览器后运行历史不丢失。
- 能从一次运行还原输入、流程、输出和验证结果。

### Milestone 3：Playbook 模板

状态：第一阶段已完成。

- 已内置默认模板 `compile-generate-verify-repair`。
- 已支持前端从模板加载 Graph。
- 后续应继续补 3 个 FDE 场景模板：
  - API 集成原型。
  - 旧系统现代化评估。
  - 缺陷复现与修复。

验收标准：

- 用户不写 DSL 也能启动一条标准交付链路。
- 模板 Graph 能编译、执行并产生可解释事件流。

### Milestone 4：交付证据包

状态：未开始。

- 增加 run export：Markdown / JSON。
- 输出变更摘要、生成文件、验证结果、失败与修复记录。
- 支持作为客户交付说明或内部复盘材料。

验收标准：

- 每次成功运行能导出一份完整交付报告。
- 报告可被人审阅，不依赖前端页面。

## 技术优先级

1. 先稳定领域模型：`Workspace`、`Playbook`、`DeliveryRun`、`RunArtifact`、`VerificationLog`。
2. 再升级持久化：从当前 JSON 文件持久化迁移到 H2/PostgreSQL 兼容实现。
3. 再重组前端信息架构：导航和页面围绕 FDE 交付对象，而不是底层技术模块。
4. 最后扩展模型 Provider：不要在领域模型未稳定时过早扩展太多模型接入。

## 建议命名

当前仓库名 `Enterprise Insight Platform` 可以保留，但产品副标题建议使用：

> FDE 交付工作台：面向 AI 辅助方案落地的可审计工程控制台。

如果后续单独产品化，可考虑：

- FDE Delivery Workbench
- FieldOps AI Workbench
- Delivery Harness Console
- Solution Engineering Control Plane

## 最近三步

1. 增加客户工作区模型，把 `DeliveryRun` 挂到 workspace 下。
2. 为每次运行生成 Markdown 交付报告，包含需求、Prompt、生成文件、验证结果和修复记录。
3. 把当前 JSON 文件持久化抽象成 repository 接口，为数据库实现做准备。
