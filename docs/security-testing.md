# Security Testing

This project treats generated code, model output, graph node prompts, and verification commands as untrusted inputs.

## Automated Coverage

- Gateway authentication: `JwtAuthFilterTest` verifies missing tokens return `401`, users without the required role return `403`, and Analyst tokens can access `/api/graph/**`.
- Prompt injection guard: `PromptCompilerTest.shouldIsolateAndNeutralizePromptInjectionInRequirement` verifies common instruction override and prompt exfiltration phrases are neutralized before prompt compilation.
- verifyCommand restriction: `ProcessProjectVerifierSecurityTest` verifies shell executables and shell-control tokens are rejected before `ProcessBuilder` runs.
- File write sandboxing: `LocalCodeFileWriterTest` and `MarkerProjectFileWriterTest` verify generated paths cannot escape `agent.ollama.output-root` or the project root.

## Manual Checks

1. Call a protected API without `Authorization`; expect `401`.
2. Call `/api/graph/run` with a valid JWT that lacks `ANALYST`; expect `403`.
3. Submit a requirement containing `Ignore previous instructions` and confirm the compiled prompt contains `[blocked instruction override]`.
4. Submit `verifyCommands` such as `["powershell", "-Command", "..."]`; expect rejection before command execution.
5. Submit generated file blocks with `../escape.txt`; expect rejection before writing.

## Policy

- Verification commands are allowlisted to build/test executables: Maven, Gradle, npm, pnpm, and Yarn families.
- Shell interpreters, path traversal, and shell-control tokens are blocked.
- Generated file paths must be relative and must stay under the configured output root.
- Prompt input is isolated and sanitized, but model safety is defense-in-depth; downstream execution must still enforce filesystem and command controls.
