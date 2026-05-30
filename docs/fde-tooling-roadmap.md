# FDE Tooling Roadmap

## 定位

将当前项目从“AI 编排控制台”收束为 Forward Deployed Engineer 的交付工作台：

> 把客户需求、系统上下文、实现方案、Agent 执行、验证结果和交付证据串成一条可复现、可审计、可复用的工程链路。

它不应该只是聊天或代码生成界面，而是 FDE 在客户现场做方案落地时的控制面板。

## 目标用户

- FDE / Solution Engineer：把客户需求快速转成可运行原型、集成方案或修复补丁。
- 平台工程师：维护可复用的交付模板、验证命令、模型路由和安全策略。
- 交付负责人：查看每次交付运行的输入、输出、验证状态、失败原因和修复记录。

## 核心工作流

```text
客户需求 / 现场上下文
  -> 需求结构化
  -> 方案图编排
  -> Prompt / Harness 编译
  -> Agent 执行
  -> 文件生成
  -> 验证命令
  -> 自动修复
  -> 交付记录与复盘
```

## 当前能力映射

| FDE 能力 | 当前模块 | 评价 |
| --- | --- | --- |
| 需求结构化 | `harness-compiler` | 已有规则型 DSL 解析，后续需要支持客户上下文、约束、验收标准。 |
| 方案编排 | `Graph Builder` / `GraphExecutor` | 已有可视化图与后端运行时，适合沉淀交付 playbook。 |
| Agent 执行 | `agent-adapter` | 已有 Ollama 适配、文件写入、验证与修复循环，需要扩展多 Provider。 |
| 运行可观测 | SSE events / timeline | 已有事件流，后续应补节点耗时、日志、产物 diff。 |
| 安全边界 | Gateway / verify allowlist / output-root | 方向正确，后续需要租户级策略与审计记录。 |
| 交付资产 | `runs` local history | 目前偏本地浏览器状态，需要服务端持久化和导出。 |

## 产品主线

### 1. Customer Workspace

面向客户或项目建立工作区，保存：

- 客户背景、系统栈、代码仓库、环境约束。
- 常用集成目标，例如 SSO、数据同步、API 网关、报表、权限模型。
- 验收标准、测试命令、部署约束。
- 交付运行历史和最终产物。

### 2. Delivery Playbook

把 Graph 从“临时流程图”升级为可复用 playbook：

- discovery：澄清需求和缺失信息。
- plan：生成实施方案与风险点。
- implement：生成或修改项目文件。
- verify：运行测试、构建、安全检查。
- repair：基于失败输出自动修复。
- handoff：生成交付说明、变更摘要、后续事项。

### 3. Evidence-Based Runs

每次运行必须留下证据：

- 输入需求、编译后的 DSL、最终 Prompt。
- 模型、参数、验证命令、工作目录。
- 事件流、节点耗时、失败原因、修复轮次。
- 生成文件列表、diff、验证日志。
- 最终状态：accepted、needs-review、failed。

### 4. Provider And Policy Layer

FDE 工具必须能适配客户环境：

- Provider：Ollama、OpenAI-compatible API、企业内部模型网关。
- Policy：允许写入路径、允许执行命令、最大修复轮次、敏感信息过滤。
- Routing：按客户、任务类型、成本、延迟和安全要求选择模型。

## MVP 打磨顺序

### Milestone 1: 交付工作台壳

- 将产品文案从 “AI Orchestrator” 调整为 “FDE Delivery Workbench” 或类似名称。
- 新增 Workspace / Playbook / Runs 三个一级概念。
- Run 页面改成以“交付运行”为中心，而不是单纯运行 DSL。
- README 增加 FDE 场景演示路径。

验收标准：

- 新用户能在 5 分钟内理解这个项目服务于 FDE 交付。
- 首页或默认页直接进入一个可运行的交付样例。

### Milestone 2: 服务端持久化运行记录

- 持久化 graph definition、run event、generated file manifest、verification log。
- Runs 页面从 localStorage 改为后端查询。
- 为每次运行生成可分享的 run detail。

验收标准：

- 刷新浏览器后运行历史不丢失。
- 能从一次 run 还原输入、流程、输出和验证结果。

### Milestone 3: Playbook 模板

- 内置 3 个 FDE 场景模板：
  - API integration prototype
  - Legacy system modernization spike
  - Bug reproduction and repair
- 支持从模板创建 Graph。
- 模板包含默认验证命令和输出格式。

验收标准：

- 用户不写 DSL 也能启动一条标准交付链路。
- 模板 Graph 能编译、执行并产生可解释事件流。

### Milestone 4: 交付证据包

- 增加 run export：Markdown / JSON。
- 输出变更摘要、生成文件、验证结果、失败与修复记录。
- 支持作为客户交付说明或内部复盘材料。

验收标准：

- 每次成功 run 能导出一份完整交付报告。
- 报告可被人审阅，不依赖前端页面。

## 技术优先级

1. 先做领域模型：`Workspace`、`Playbook`、`DeliveryRun`、`RunArtifact`、`VerificationLog`。
2. 再做持久化：优先 H2/PostgreSQL 兼容，避免只依赖浏览器状态。
3. 再做前端重组：导航和页面围绕 FDE 交付对象，不围绕底层技术模块。
4. 最后扩展模型 Provider：不要在领域模型未稳定时过早扩展太多模型接入。

## 建议改名

当前名称 `Enterprise Insight Platform` 偏宽泛，不容易表达 FDE 工具属性。可选方向：

- FDE Delivery Workbench
- FieldOps AI Workbench
- Delivery Harness Console
- Solution Engineering Control Plane

如果希望保留原名，可以把产品副标题改成：

> Enterprise Insight Platform: FDE Delivery Workbench for AI-assisted implementation.

## 最近可做的三件事

1. 增加 `DeliveryRun` 后端模型和持久化接口，把当前 run history 从前端 localStorage 下沉到服务端。
2. 将默认图从 `compile -> generate -> verify -> repair` 包装成第一个 Playbook 模板。
3. 改造首页和导航文案，让默认体验从“编辑 DSL”转为“选择客户工作区并运行交付 playbook”。
