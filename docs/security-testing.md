# 安全测试说明

## 范围

FDE 交付工作台会处理模型输入、模型输出、生成文件、验证命令和客户项目代码。所有这些内容都必须按不可信输入处理。

安全测试覆盖：

- Gateway 鉴权。
- 角色访问控制。
- Prompt 注入防护。
- 验证命令白名单。
- 文件写入沙箱。
- 项目扫描范围控制。
- DeliveryRun 证据持久化边界。

## 自动化覆盖

| 范围 | 测试 |
| --- | --- |
| Gateway 鉴权 | `JwtAuthFilterTest` 验证无 token 返回 `401`，角色不足返回 `403`。 |
| Prompt 注入防护 | `PromptCompilerTest.shouldIsolateAndNeutralizePromptInjectionInRequirement` 验证常见指令覆盖和 Prompt 泄露语句会被中和。 |
| 验证命令限制 | `ProcessProjectVerifierSecurityTest` 验证 shell 解释器和 shell 控制符在执行前被拒绝。 |
| 文件写入沙箱 | `LocalCodeFileWriterTest` 和 `MarkerProjectFileWriterTest` 验证生成路径不能逃逸输出根目录。 |
| DeliveryRun 持久化 | `DeliveryRunStoreTest` 验证运行请求、事件和终态响应被写入服务端记录。 |
| 项目扫描 | `ProjectScannerServiceTest` 验证扫描结果只从配置根目录提取证据。 |

## 手工检查

1. 不携带 `Authorization` 调用受保护接口，预期返回 `401`。
2. 使用没有 `ANALYST` 权限的 JWT 调用 `/api/graph/run`，预期返回 `403`。
3. 提交包含 `Ignore previous instructions` 的需求，确认编译后的 Prompt 不会直接执行该指令。
4. 提交 `["powershell", "-Command", "..."]` 之类的验证命令，预期执行前被拒绝。
5. 提交包含 `../escape.txt` 的生成文件路径，预期写入前被拒绝。
6. 配置不存在的 `EIP_PROJECT_ANALYSIS_ROOT`，预期项目扫描接口返回明确错误。
7. 启动一次 DeliveryRun 后刷新浏览器，确认 Run Evidence 仍能从服务端读取运行记录。

## 验证命令策略

验证命令只允许构建和测试工具，例如：

- Maven
- Gradle
- npm
- pnpm
- Yarn

以下输入必须拒绝：

- shell 解释器。
- 管道。
- 重定向。
- 命令连接符。
- 路径穿越。
- 动态拼接出的危险命令。

## 文件写入策略

生成文件必须满足：

- 路径是相对路径。
- 路径规范化后仍在输出根目录内。
- 不允许写入系统目录。
- 不允许覆盖项目外文件。

## Prompt 输入策略

Prompt 输入会被视为用户提供的需求内容，而不是系统指令。编译器应保留业务语义，但必须削弱常见的指令覆盖和 Prompt 泄露语句。

安全边界不能只依赖 Prompt 清洗。文件系统、验证命令和服务端 API 仍必须独立做限制。

## 项目扫描策略

项目扫描属于只读能力，但仍要控制范围：

- 默认忽略 `.git`、`.idea`、`.mvn`、`node_modules`、`target`、`dist`、`build`、`runtime-logs`、`output`。
- 通过 `max-depth` 和 `max-files` 限制扫描成本。
- 通过 `max-evidence-per-category` 限制返回证据数量。
- 不执行项目代码。
- 不执行扫描到的脚本。
- 不读取二进制文件。

## DeliveryRun 证据策略

DeliveryRun 记录会保存运行请求、事件和最终响应，因此后续必须继续增强：

- 敏感字段脱敏。
- 租户级访问控制。
- 证据包导出审计。
- 数据库存储加密或字段级保护。
- 运行记录保留周期配置。
