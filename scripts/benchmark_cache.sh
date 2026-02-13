#!/usr/bin/env bash

set -euo pipefail

# Simple benchmark script to compare pre/post cache/query optimization.
# Usage:
#   ./scripts/benchmark_cache.sh baseline
#   ./scripts/benchmark_cache.sh optimized
#
# Run it against a running app (dev profile) and compare the generated reports.

LABEL="${1:-run}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
ITERATIONS="${ITERATIONS:-50}"

REPORT_DIR="metrics"
mkdir -p "$REPORT_DIR"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
REPORT_FILE="$REPORT_DIR/cache_benchmark_${LABEL}_${TIMESTAMP}.txt"

echo "Benchmark label : $LABEL" | tee "$REPORT_FILE"
echo "Base URL        : $BASE_URL" | tee -a "$REPORT_FILE"
echo "Iterations/ep   : $ITERATIONS" | tee -a "$REPORT_FILE"
echo "Started at      : $(date -Iseconds)" | tee -a "$REPORT_FILE"
echo "" | tee -a "$REPORT_FILE"

bench_endpoint() {
  local name="$1"
  local method="$2"
  local url="$3"
  local body="${4:-}"

  echo "Running benchmark: $name ($method $url)" | tee -a "$REPORT_FILE"

  local start_s end_s total_ms avg_ms
  start_s=$(date +%s)

  for i in $(seq 1 "$ITERATIONS"); do
    if [[ "$method" == "GET" ]]; then
      http_code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL$url")
    else
      http_code=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Content-Type: application/json" \
        -d "$body" \
        "$BASE_URL$url")
    fi
    if [[ "$http_code" -ge 500 ]]; then
      echo "  Request $i failed with HTTP $http_code, aborting this benchmark." | tee -a "$REPORT_FILE"
      break
    fi
  done

  end_s=$(date +%s)
  total_ms=$(((end_s - start_s) * 1000))
  avg_ms=$((total_ms / ITERATIONS))

  printf "  Total time : %d ms for %d requests\n" "$total_ms" "$ITERATIONS" | tee -a "$REPORT_FILE"
  printf "  Avg/req    : %d ms\n\n" "$avg_ms" | tee -a "$REPORT_FILE"
}

# REST endpoints focused on query optimization and caching

# 1. List posts with filters (hits PostService.getPosts + cache)
bench_endpoint "REST: list posts (published, page 0 size 10)" \
  "GET" "/api/posts?page=0&size=10&publishedOnly=true"

bench_endpoint "REST: list posts (search=GraphQL)" \
  "GET" "/api/posts?page=0&size=10&search=GraphQL&publishedOnly=true"

# 2. Post detail by id and slug (hits PostService.getPost / getPostBySlug + cache)
bench_endpoint "REST: get post by id (1)" \
  "GET" "/api/posts/1"

bench_endpoint "REST: get post by slug (getting-started-with-spring-boot)" \
  "GET" "/api/posts/slug/getting-started-with-spring-boot"

# 3. Tags and users (TagService/UserService caches)
bench_endpoint "REST: list tags (page 0 size 10)" \
  "GET" "/api/tags?page=0&size=10"

bench_endpoint "REST: list users (page 0 size 10)" \
  "GET" "/api/users?page=0&size=10"

# 4. GraphQL posts query (uses same service methods under GraphQL)
GRAPHQL_POSTS_BODY='{"query":"{ posts(page: 0, size: 10, publishedOnly: true) { id title slug status } }"}'
bench_endpoint "GraphQL: posts list (publishedOnly)" \
  "POST" "/graphql" "$GRAPHQL_POSTS_BODY"

echo "Finished at      : $(date -Iseconds)" | tee -a "$REPORT_FILE"
echo "Report written to: $REPORT_FILE"

