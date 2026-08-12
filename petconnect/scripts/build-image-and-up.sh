#!/usr/bin/env bash
set -euo pipefail

SKIP_TESTS=false
if [[ "${1:-}" == "--skip-tests" ]]; then
  SKIP_TESTS=true
fi

# Find mvn: prefer system mvn, else use mvnw if present
MVN_BIN=""
if command -v mvn >/dev/null 2>&1; then
  MVN_BIN="mvn"
elif [[ -x "./mvnw" ]]; then
  MVN_BIN="./mvnw"
else
  echo "Error: neither 'mvn' nor './mvnw' found. Install Maven or include the Maven wrapper in the repo." >&2
  exit 2
fi

if $SKIP_TESTS; then
  MVN_CMD="$MVN_BIN clean package -DskipTests"
else
  MVN_CMD="$MVN_BIN clean package"
fi

echo "Running: $MVN_CMD"
eval "$MVN_CMD"

echo "Building Docker image petconnect:latest"
docker build -t petconnect:latest .

echo "Starting docker-compose services"
docker-compose up -d

echo "Done. Use 'docker-compose logs -f' to follow logs."
