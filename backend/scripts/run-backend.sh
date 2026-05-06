#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
REPO_ROOT="$(cd "${BACKEND_DIR}/.." && pwd)"
LOG_DIR="${EIP_LOG_DIR:-${REPO_ROOT}/runtime-logs}"
START_DELAY="${EIP_BACKEND_START_DELAY:-2}"

mkdir -p "${LOG_DIR}"

PIDS=()

cleanup() {
  if ((${#PIDS[@]} > 0)); then
    echo "Stopping backend services..."
    kill "${PIDS[@]}" 2>/dev/null || true
    wait "${PIDS[@]}" 2>/dev/null || true
  fi
}

trap cleanup INT TERM EXIT

if [[ "${EIP_SKIP_BACKEND_BUILD:-0}" != "1" ]]; then
  echo "Building backend modules with tests skipped..."
  (cd "${BACKEND_DIR}" && mvn -DskipTests install)
fi

GATEWAY_ARGS=(
  "--spring.cloud.gateway.routes[0].id=auth-service"
  "--spring.cloud.gateway.routes[0].uri=http://localhost:8101"
  "--spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**"
  "--spring.cloud.gateway.routes[1].id=metadata-service"
  "--spring.cloud.gateway.routes[1].uri=http://localhost:8102"
  "--spring.cloud.gateway.routes[1].predicates[0]=Path=/api/metadata/**"
  "--spring.cloud.gateway.routes[2].id=ai-service"
  "--spring.cloud.gateway.routes[2].uri=http://localhost:8104"
  "--spring.cloud.gateway.routes[2].predicates[0]=Path=/api/ai/**"
  "--spring.cloud.gateway.routes[3].id=harness-compiler"
  "--spring.cloud.gateway.routes[3].uri=http://localhost:8090"
  "--spring.cloud.gateway.routes[3].predicates[0]=Path=/api/compiler/**"
  "--spring.cloud.gateway.routes[4].id=agent-adapter"
  "--spring.cloud.gateway.routes[4].uri=http://localhost:8088"
  "--spring.cloud.gateway.routes[4].predicates[0]=Path=/api/agent-adapter/**"
  "--spring.cloud.gateway.routes[5].id=orchestrator-service"
  "--spring.cloud.gateway.routes[5].uri=http://localhost:8091"
  "--spring.cloud.gateway.routes[5].predicates[0]=Path=/api/orchestrator/**"
  "--spring.cloud.gateway.routes[6].id=graph-runtime"
  "--spring.cloud.gateway.routes[6].uri=http://localhost:8091"
  "--spring.cloud.gateway.routes[6].predicates[0]=Path=/api/graph/**"
)

start_service() {
  local module="$1"
  shift || true
  local log_file="${LOG_DIR}/${module}.log"

  echo "Starting ${module}; log: ${log_file}"
  (cd "${BACKEND_DIR}" && mvn -pl "${module}" spring-boot:run "$@") >"${log_file}" 2>&1 &
  PIDS+=("$!")
  sleep "${START_DELAY}"
}

start_service "gateway-service" "-Dspring-boot.run.arguments=${GATEWAY_ARGS[*]}"
start_service "auth-service"
start_service "metadata-service"
start_service "ai-service"
start_service "harness-compiler"
start_service "agent-adapter"
start_service "orchestrator-service"

cat <<'INFO'

Backend services are starting:
  Gateway              http://localhost:8080
  Auth Service         http://localhost:8101
  Metadata Service     http://localhost:8102
  AI Service           http://localhost:8104
  Harness Compiler     http://localhost:8090
  Agent Adapter        http://localhost:8088
  Orchestrator         http://localhost:8091

Press Ctrl+C to stop backend processes started by this script.
INFO

wait "${PIDS[@]}"
