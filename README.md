# Enterprise Insight Platform

Enterprise Insight Platform is an AI workflow engineering platform for compiling requirements into structured DSL, prompts, executable graph workflows, and generated projects. It is designed as a controllable harness around LLM execution: the UI builds workflows, backend services compile and execute them, and the runtime emits observable events for debugging and profiling.

## Positioning

This project is not a generic chat application. It is an engineering control plane for AI-assisted delivery:

- Requirement -> DSL -> prompt -> generated project.
- Visual graph builder -> validated graph definition -> event-driven graph runtime.
- Agent adapter -> file writer -> verification command runner -> auto-repair loop.
- Gateway authentication and role checks around compiler, graph, orchestrator, agent, and AI APIs.

The main design goal is reproducibility: every generated artifact should be traced back to a DSL model, prompt, graph definition, run id, verification command, and emitted runtime event.

## Architecture

```text
React Console
  |-- DSL Editor
  |-- Graph Builder (React Flow)
  |-- Run Console / Event Timeline
  |
  v
Gateway Service :8080
  |-- JWT auth
  |-- role routing
  |
  +--> Auth Service :808x
  +--> Metadata Service
  +--> Harness Compiler
  |      |-- RuleBasedDslParser
  |      |-- GraphToDslCompiler
  |      |-- PromptCompiler
  |      '-- Prompt injection guard
  |
  +--> Orchestrator Service :8091
  |      |-- Orchestrator pipeline
  |      |-- Graph compile API
  |      |-- GraphExecutor
  |      |-- SSE GraphEvent stream
  |      '-- Experiment assignment kernel
  |
  '--> Agent Adapter
         |-- Ollama adapter
         |-- generated file writers
         |-- verifyCommand allowlist
         '-- auto-repair loop
```

## Core Capabilities

### Debug

- Graph runtime emits `GRAPH_RUN_STARTED`, `NODE_STARTED`, `NODE_SUCCEEDED`, `NODE_FAILED`, `EDGE_TRAVERSED`, `GRAPH_RUN_COMPLETED`, and `GRAPH_RUN_FAILED`.
- SSE stream endpoint supports replay with `Last-Event-ID` or `lastEventId`.
- The React runtime store renders node and edge state from events only; execution logic stays in Spring Boot.
- API contract tests lock response envelope shape for orchestrator and graph APIs.

### Profiling

- k6 load script reports latency, RPS, and error rate for compiler and graph runtime endpoints.
- Graph execution events expose node and edge traversal, making per-node timing instrumentation straightforward.
- Verification command results retain exit code, timeout status, stdout/stderr, and duration.

### AB Test / Experiment

- `ExperimentAssignmentService` provides deterministic weighted assignment by `experimentKey` and `subjectKey`.
- Variant assignment is stable for the same subject, which supports prompt template experiments, model routing, or repair-policy comparisons.
- Experiment tests cover deterministic assignment, zero-weight variants, and invalid definitions.

## Services

| Service | Responsibility | Key APIs |
| --- | --- | --- |
| `gateway-service` | JWT auth, role enforcement, service routing | `/api/**` |
| `auth-service` | demo login/profile and JWT issue | `POST /api/auth/login` |
| `harness-compiler` | requirement/graph to DSL and prompt | `POST /api/compiler/compile`, `POST /api/compiler/from-graph` |
| `orchestrator-service` | compile -> generate -> verify pipeline and graph runtime | `POST /api/orchestrator/run`, `POST /api/graph/compile`, `POST /api/graph/run`, `GET /api/graph/run/stream/{runId}` |
| `agent-adapter` | LLM call, project writing, verification, repair loop | `POST /api/agent-adapter/**` |

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 20+
- npm
- Optional: k6 for load testing
- Optional: Ollama if running real local LLM generation

### Backend

From the repository root:

```powershell
cd backend
mvn test
```

Run the platform with the project scripts:

```powershell
.\scripts\run-harness.ps1
```

The gateway defaults to `http://localhost:8080`. The orchestrator service defaults to port `8091`.

### Frontend

```powershell
cd enterprise-insight-backend-react
npm install
npm run dev -- --host 127.0.0.1
```

Open the printed Vite URL and navigate to `/graph` for the visual graph builder.

### Authentication

Login through the gateway:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

Use `data.token` as `Authorization: Bearer <token>` for protected APIs.

## Testing

### Unit Tests

Run all backend tests:

```powershell
cd backend
mvn test
```

Coverage added in this project:

- Orchestrator: service-level compile-before-agent execution and full pipeline integration.
- Compiler: DSL parsing, prompt compilation, graph-to-DSL prompt compilation, prompt template rendering, prompt injection guard.
- Experiment: deterministic weighted variant assignment.
- Agent adapter: auto-repair loop, Ollama adapter behavior, generated file path sandboxing, verifyCommand restrictions.
- Gateway: JWT auth, role checks, graph route protection.

### Integration Tests

`PipelineIntegrationTest` exercises the local pipeline without a real LLM:

```text
requirement
  -> RuleBasedDslParser
  -> PromptCompiler
  -> DefaultOrchestratorService
  -> DefaultAutoRepairGenerationService
  -> MarkerProjectFileWriter
  -> ProjectVerifier
```

It verifies DSL module selection, prompt security controls, generated file writing, and verification command propagation.

### API Contract Tests

Contract tests lock the current API surface:

- `OrchestratorControllerContractTest`
  - `POST /api/orchestrator/run`
  - validates API envelope and bad request shape.
- `GraphApiContractTest`
  - `POST /api/graph/compile`
  - `POST /api/graph/run`
  - validates graph run response and invalid graph rejection.
- `CompileControllerTest`
  - `POST /api/compiler/compile`
  - `POST /api/compiler/from-graph`
  - validates DSL/prompt response shape and validation errors.

### Current Verification Result

Latest local verification in this workspace:

```text
mvn test                         PASS
npm run build                    PASS
k6 version                       PASS - k6.exe v1.7.1
```

The frontend build may require normal OS execution permissions for Vite/esbuild on Windows.

## Performance

k6 script:

```text
scripts/k6/graph-runtime-load.js
```

Run against the gateway:

```powershell
k6 run scripts/k6/graph-runtime-load.js
```

Common parameters:

```powershell
$env:BASE_URL="http://localhost:8080"
$env:VUS="25"
$env:RAMP_UP="30s"
$env:STEADY="2m"
$env:RAMP_DOWN="30s"
k6 run scripts/k6/graph-runtime-load.js
```

Run directly against `orchestrator-service` when gateway/Nacos is not running:

```powershell
$env:BASE_URL="http://localhost:8091"
$env:AUTH_MODE="none"
$env:SKIP_COMPILER="true"
$env:VUS="10"
$env:RAMP_UP="30s"
$env:STEADY="1m"
$env:RAMP_DOWN="20s"
k6 run scripts/k6/graph-runtime-load.js
```

Use an existing token instead of login:

```powershell
$env:TOKEN="<jwt>"
k6 run scripts/k6/graph-runtime-load.js
```

Enable the heavy orchestrator pipeline path only when the agent adapter and model runtime are ready:

```powershell
$env:RUN_ORCHESTRATOR="true"
k6 run scripts/k6/graph-runtime-load.js
```

The script prints and writes:

- latency: `avgMs`, `p95Ms`, `p99Ms`
- RPS: `http_reqs.rate`
- error rate: `http_req_failed.rate`
- output file: `performance-results.json`

Current local performance execution result:

```text
date                             2026-05-04
scope                            graph runtime direct mode
baseUrl                          http://localhost:8091
authMode                         none
skipCompiler                     true
vus                              10
rampUp                           30s
steady                           1m
rampDown                         20s
requests                         1726
rps                              15.66
errorRate                        0
latency.avgMs                    0.86
latency.p95Ms                    1.90
latency.p99Ms                    3.18
thresholds                       PASS
```

Default thresholds:

```text
http_req_failed < 1%
http_req_duration p95 < 800ms
http_req_duration p99 < 1500ms
graph_compile_latency p95 < 500ms
graph_run_latency p95 < 800ms
compiler_latency p95 < 700ms
contract_failures < 1%
```

## Security Testing

Security test details are documented in [docs/security-testing.md](docs/security-testing.md).

Implemented controls:

- Gateway rejects protected APIs without JWT.
- Gateway enforces `ANALYST` role for compiler, graph, orchestrator, agent, metadata, and AI routes.
- Prompt compiler isolates untrusted input and neutralizes common instruction override and prompt exfiltration phrases.
- `verifyCommand` execution is restricted to build/test executable allowlists.
- Shell interpreters, shell-control tokens, and path traversal are rejected before `ProcessBuilder` starts.
- Generated files must stay inside `agent.ollama.output-root` and the selected project directory.

## Graph Runtime Contract

Graph definitions must satisfy:

- exactly one `start` node
- at least one `end` node
- condition nodes must expose success and failed/failure branches
- the graph must be a DAG after bounded loop edges are removed
- loop edges must use `maxIterations`

The backend owns execution. The UI only emits graph definitions and renders GraphEvents.

## Roadmap

- Persist graph definitions and run event logs.
- Add per-node duration metrics and profiling export.
- Promote experiment assignments into orchestrator request metadata.
- Add OpenAPI generation and schema diff contract checks.
- Add k6 cloud/CI performance baseline publishing.
- Add containerized integration tests for gateway + compiler + orchestrator.
- Add policy-driven verifyCommand profiles per tenant.
- Add richer prompt injection detection with structured risk scoring.

## License

MIT
