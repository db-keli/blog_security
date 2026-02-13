#!/usr/bin/env bash

set -euo pipefail

# Orchestrate baseline (no cache) and optimized (with cache) benchmarks.
# This script:
# - Starts the app with cache disabled, runs benchmark_cache.sh baseline-nocache, stops the app.
# - Starts the app with cache enabled,   runs benchmark_cache.sh optimized-cache, stops the app.

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f ".env" ]]; then
  echo ".env file not found in $ROOT_DIR; please create it with DB_* vars."
  exit 1
fi

start_app() {
  local label="$1"
  local cache_type="$2"  # e.g. 'none' or 'simple'

  echo "=== Starting app for $label (spring.cache.type=$cache_type) ==="
  set -a
  source .env
  set +a

  SPRING_CACHE_ARG=""
  if [[ -n "$cache_type" ]]; then
    SPRING_CACHE_ARG="-Dspring.cache.type=$cache_type"
  fi

  mvn -q spring-boot:run -Dspring-boot.run.profiles=dev $SPRING_CACHE_ARG &
  APP_PID=$!

  # Wait for app to start
  echo "Waiting for app to start (PID $APP_PID)..."
  sleep 25
}

stop_app() {
  local pid="$1"
  if kill -0 "$pid" 2>/dev/null; then
    echo "Stopping app (PID $pid)..."
    kill "$pid" || true
    sleep 5
  fi
}

run_benchmark() {
  local label="$1"
  echo "=== Running benchmark: $label ==="
  ./scripts/benchmark_cache.sh "$label"
}

main() {
  # Baseline: no cache
  start_app "baseline-nocache" "none"
  run_benchmark "baseline-nocache"
  stop_app "$APP_PID"

  # Optimized: with cache (default cache type)
  start_app "optimized-cache" ""
  run_benchmark "optimized-cache"
  stop_app "$APP_PID"

  echo "Benchmarks completed. Reports are in metrics/cache_benchmark_*.txt"
}

main "$@"

