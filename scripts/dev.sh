#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
BACKEND_DIR="${REPO_ROOT}/backend"
FRONTEND_DIR="${REPO_ROOT}/enterprise-insight-backend-react"
LOG_DIR="${EIP_LOG_DIR:-${REPO_ROOT}/runtime-logs}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"

mkdir -p "${LOG_DIR}"

PIDS=()

cleanup() {
  if ((${#PIDS[@]} > 0)); then
    echo "Stopping dev processes..."
    kill "${PIDS[@]}" 2>/dev/null || true
    wait "${PIDS[@]}" 2>/dev/null || true
  fi
}

trap cleanup INT TERM EXIT

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
    return
  fi

  if command -v docker-compose >/dev/null 2>&1; then
    docker-compose "$@"
    return
  fi

  echo "Docker Compose is required but was not found." >&2
  exit 1
}

echo "Starting base services with Docker Compose..."
compose -f "${BACKEND_DIR}/compose.yaml" up -d

echo "Starting backend services..."
bash "${BACKEND_DIR}/scripts/run-backend.sh" &
PIDS+=("$!")

if [[ ! -d "${FRONTEND_DIR}/node_modules" ]]; then
  echo "Installing frontend dependencies..."
  (cd "${FRONTEND_DIR}" && npm install)
fi

echo "Starting frontend dev server..."
(cd "${FRONTEND_DIR}" && npm run dev -- --host 127.0.0.1 --port "${FRONTEND_PORT}" --strictPort) >"${LOG_DIR}/frontend.log" 2>&1 &
PIDS+=("$!")

cat <<INFO

Development environment is starting:
  Frontend             http://127.0.0.1:${FRONTEND_PORT}
  Gateway API          http://localhost:8080
  Orchestrator API     http://localhost:8091
  Qdrant               http://localhost:6333
  Ollama               http://localhost:11434

Logs:
  ${LOG_DIR}

Login:
  username: admin
  password: admin

Press Ctrl+C to stop frontend/backend processes. Docker services remain running;
stop them with: docker compose -f backend/compose.yaml down
INFO

wait "${PIDS[@]}"
