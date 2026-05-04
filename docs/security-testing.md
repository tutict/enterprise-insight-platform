# Security Testing / 安全测试

## 范围 / Scope

本项目将生成代码、模型输出、Graph 节点 Prompt、Tool 配置和 `verifyCommand` 都视为不可信输入。安全测试覆盖鉴权、Prompt injection、防止危险命令执行，以及生成文件路径限制。

This project treats generated code, model output, graph node prompts, tool configuration, and `verifyCommand` as untrusted inputs. Security testing covers authentication, prompt injection, dangerous command prevention, and generated file path restrictions.

## 自动化覆盖 / Automated Coverage

- Gateway authentication / 网关鉴权：`JwtAuthFilterTest` 验证无 token 返回 `401`，角色不足返回 `403`，`ANALYST` 可以访问 `/api/graph/**`。
- Prompt injection guard / Prompt 注入防护：`PromptCompilerTest.shouldIsolateAndNeutralizePromptInjectionInRequirement` 验证常见指令覆盖和系统 Prompt 泄露语句会在编译前被中和。
- verifyCommand restriction / 验证命令限制：`ProcessProjectVerifierSecurityTest` 验证 shell 解释器和 shell 控制符在 `ProcessBuilder` 执行前被拒绝。
- File write sandboxing / 文件写入沙箱：`LocalCodeFileWriterTest` 和 `MarkerProjectFileWriterTest` 验证生成路径不能逃逸 `agent.ollama.output-root` 或项目根目录。

- Gateway authentication: `JwtAuthFilterTest` verifies missing tokens return `401`, users without the required role return `403`, and `ANALYST` users can access `/api/graph/**`.
- Prompt injection guard: `PromptCompilerTest.shouldIsolateAndNeutralizePromptInjectionInRequirement` verifies common instruction override and prompt exfiltration phrases are neutralized before prompt compilation.
- verifyCommand restriction: `ProcessProjectVerifierSecurityTest` verifies shell executables and shell-control tokens are rejected before `ProcessBuilder` runs.
- File write sandboxing: `LocalCodeFileWriterTest` and `MarkerProjectFileWriterTest` verify generated paths cannot escape `agent.ollama.output-root` or the project root.

## 手工检查 / Manual Checks

1. 未携带 `Authorization` 调用受保护接口，预期返回 `401`。
2. 使用不具备 `ANALYST` 角色的 JWT 调用 `/api/graph/run`，预期返回 `403`。
3. 提交包含 `Ignore previous instructions` 的需求，确认编译后的 Prompt 包含 `[blocked instruction override]`。
4. 提交 `["powershell", "-Command", "..."]` 之类的 `verifyCommands`，预期执行前被拒绝。
5. 提交包含 `../escape.txt` 的生成文件块，预期写入前被拒绝。

1. Call a protected API without `Authorization`; expect `401`.
2. Call `/api/graph/run` with a JWT that lacks `ANALYST`; expect `403`.
3. Submit a requirement containing `Ignore previous instructions` and confirm the compiled prompt contains `[blocked instruction override]`.
4. Submit `verifyCommands` such as `["powershell", "-Command", "..."]`; expect rejection before command execution.
5. Submit generated file blocks with `../escape.txt`; expect rejection before writing.

## 策略 / Policy

- 验证命令白名单仅包含构建和测试工具：Maven、Gradle、npm、pnpm、Yarn。
- shell 解释器、路径穿越、管道和重定向等 shell 控制符会被拒绝。
- 生成文件路径必须是相对路径，并且必须保持在配置的输出根目录下。
- Prompt 输入会隔离和清洗，但模型安全仍然需要下游文件系统与命令执行控制共同兜底。

- Verification commands are allowlisted to build/test executables: Maven, Gradle, npm, pnpm, and Yarn families.
- Shell interpreters, path traversal, pipes, redirects, and shell-control tokens are blocked.
- Generated file paths must be relative and must stay under the configured output root.
- Prompt input is isolated and sanitized, but model safety is defense-in-depth; downstream execution must still enforce filesystem and command controls.
