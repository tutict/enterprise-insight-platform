# FDE 交付工作台路线图

## 产品定位

FDE 交付工作台面向 Forward Deployed Engineer 的真实交付流程。它不是“AI 编排器”的通用演示，而是一个帮助工程师在客户现场完成发现、分析、实现、验证和交付复盘的工作台。

核心判断是：AI 只有理解行业资料、客户业务、现有系统和代码证据，才可能精准落在最急需的业务能力上。因此工具链必须先建立项目理解，再驱动交付运行。

## 目标用户

- FDE / 解决方案工程师：把客户需求、行业约束和现有系统快速转成可验证的实现切片。
- 平台工程师：维护 Playbook、验证命令、模型路由、安全策略和运行证据存储。
- 交付负责人：查看每次交付运行的输入、输出、失败原因、修复记录和验证结果。
- 企业技术负责人：评估 AI 交付是否可复现、可审计、可治理。

## 端到端流程

```text
行业资料收集
  -> 客户业务分析
  -> 现有项目代码扫描
  -> 业务能力候选识别
  -> 交付机会排序
  -> 生成交付简报
  -> 装载 Playbook
  -> 启动 DeliveryRun
  -> 生成 / 修改代码
  -> 验证 / 修复
  -> 沉淀交付证据
```

## 当前能力状态

| 能力 | 状态 | 说明 |
| --- | --- | --- |
| FDE 产品收束 | 已完成第一阶段 | 前端导航和文案已从 AI Orchestrator 收束为 FDE Delivery Workbench。 |
| 默认交付 Playbook | 已完成第一阶段 | `compile-generate-verify-repair` 已内置为默认交付模板。 |
| 行业与业务发现 Playbook | 已完成第一阶段 | `industry-business-discovery` 已内置，用于发现、分析、建模和交付 backlog。 |
| DeliveryRun 服务端持久化 | 已完成第一阶段 | 请求、事件、响应和状态写入 `runtime-logs/delivery-runs/*.json`。 |
| Run Evidence 页面 | 已完成第一阶段 | 前端从服务端读取运行记录，不再依赖浏览器 localStorage。 |
| 项目理解扫描 | 已完成第一阶段 | 后端扫描模块、API、前端路由、文档、测试和能力候选。 |
| 交付简报 | 已完成第一阶段 | 项目扫描结果可生成 run-ready requirement，并一键装载到 Delivery Run。 |
| 客户工作区 | 未开始 | 需要将客户、仓库、项目扫描、运行记录和证据包挂到 Workspace 下。 |
| 证据包导出 | 未开始 | 需要支持 Markdown/JSON 导出。 |

## 项目理解能力

项目理解是 FDE 工作台区别于普通代码生成器的关键能力。

当前扫描器会读取配置中的项目根目录，默认通过 `EIP_PROJECT_ANALYSIS_ROOT` 或当前工作目录向上查找仓库根目录。扫描内容包括：

- Java Controller 映射，用于识别后端 API 行为。
- React 路由，用于识别用户侧工作流入口。
- Markdown 文档，用于识别已有业务、架构、需求和设计材料。
- 测试文件，用于判断当前验证覆盖情况。
- 模块边界，例如 Maven 模块、前端包、文档目录。

扫描结果会被聚合为：

- `ProjectInventory`：项目清单。
- `BusinessCapability`：业务能力候选。
- `DeliveryOpportunity`：交付机会。
- `ProjectDeliveryBrief`：可直接装载到 Delivery Run 的交付简报。

## 交付简报设计

交付简报是“项目理解”和“AI 执行”之间的桥。它不是简单摘要，而是可运行输入。

交付简报包含：

- 交付目标。
- 优先级和机会点。
- 项目结构统计。
- 业务能力候选。
- API、路由、文档、测试等代码证据。
- 建议使用的 Playbook。
- 默认目标目录。
- 默认验证命令。
- 修复轮次。
- 可传给运行器的 options 元数据。

前端 Project Intel 页面点击“装载到交付运行”后，会把简报写入运行页输入区，并带入 `targetDirectory`、`verifyCommand` 和 `maxRepairRounds`。

## Playbook 主线

### 1. `compile-generate-verify-repair`

默认交付闭环：

```text
compile -> generate -> verify -> repair
```

适用于明确需求已经形成、需要生成或修改工程文件并验证结果的场景。

### 2. `industry-business-discovery`

发现与分析闭环：

```text
collect -> analyze -> model -> recommend
```

适用于前期调研，需要收集行业资料、分析现有业务、抽取领域模型、形成交付 backlog 的场景。

### 3. 后续 Playbook 候选

- API 集成原型：从外部接口文档生成集成服务和验证用例。
- 遗留系统现代化评估：扫描旧系统模块，输出分阶段改造建议。
- 缺陷复现与修复：从错误日志、测试失败和代码证据生成最小修复切片。
- 数据同步与报表：围绕数据模型、同步任务和校验规则生成交付计划。

## 里程碑

### Milestone 1：FDE 工作台壳层

状态：已完成第一阶段。

验收标准：

- 新用户能在 5 分钟内理解这是 FDE 交付工具，而不是通用聊天工具。
- 默认页面能进入一个可运行交付流程。
- Playbooks、Delivery Run、Run Evidence、Project Intel 的导航语义清晰。

### Milestone 2：DeliveryRun 持久化

状态：已完成第一阶段。

验收标准：

- 刷新浏览器后运行历史不丢失。
- 可从一次运行还原输入、事件、输出和验证结果。
- 运行终态可用于审计和复盘。

### Milestone 3：项目理解与交付简报

状态：已完成第一阶段。

验收标准：

- 后端可扫描现有仓库并返回项目清单。
- 项目清单能识别 API、路由、文档、测试和业务能力候选。
- 系统能生成带证据的交付简报。
- 前端可一键把交付简报装载到 Delivery Run。

### Milestone 4：工作区与证据包

状态：未开始。

验收标准：

- 每个客户或项目有独立 Workspace。
- DeliveryRun 挂到 Workspace 下。
- 每次成功运行可导出 Markdown/JSON 交付报告。
- 报告包含需求、Prompt、生成文件、验证命令、验证结果、失败与修复记录。

### Milestone 5：数据库化与多租户治理

状态：未开始。

验收标准：

- DeliveryRun 从 JSON 文件迁移到 H2/PostgreSQL。
- 验证命令、模型 Provider、输出目录和敏感信息策略支持租户级配置。
- 运行证据可按 Workspace、Playbook、状态和时间检索。

## 优先级建议

1. 先稳定领域模型：`Workspace`、`ProjectInventory`、`ProjectDeliveryBrief`、`Playbook`、`DeliveryRun`、`RunArtifact`、`VerificationLog`。
2. 再增强项目理解：补充 package/import 依赖图、数据库迁移文件、配置文件、测试覆盖率和最近变更分析。
3. 再做证据包导出：从当前 DeliveryRun 记录生成 Markdown 和 JSON。
4. 再做数据库持久化：用 repository 抽象替换文件读写。
5. 最后扩展 Provider 和策略层，避免领域模型未稳定时过早接入太多模型能力。

## 最近三步

1. 把项目扫描结果接入 Discovery Playbook 的运行上下文，让发现流程直接使用代码证据。
2. 增加 Markdown 交付证据包导出，覆盖简报、运行请求、事件、最终响应和验证结果。
3. 增加 Workspace 模型，把项目扫描、DeliveryRun 和导出报告统一挂载到客户或项目空间下。
