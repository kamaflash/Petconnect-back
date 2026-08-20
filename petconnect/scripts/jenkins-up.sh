#!/usr/bin/env bash
# Build y levanta el contenedor Jenkins con cliente Docker incluido.
# Uso (desde la raíz del proyecto):
#   ./scripts/jenkins-up.sh                          # build + run
#   ./scripts/jenkins-up.sh --network jenkins-net    # red personalizada
#   ./scripts/jenkins-up.sh --no-recreate            # no eliminar contenedor existente
#   ./scripts/jenkins-up.sh --no-restart             # sin --restart unless-stopped
set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-jenkins-with-docker}"
TAG="${TAG:-latest}"
NETWORK="${NETWORK:-jenkins-net}"
PORT="${PORT:-8080}"
JNLP_PORT="${JNLP_PORT:-50000}"
NO_RECREATE=0
RESTART_OPT="--restart unless-stopped"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --image)
      IMAGE_NAME="$2"; shift 2 ;;
    --tag)
      TAG="$2"; shift 2 ;;
    --network)
      NETWORK="$2"; shift 2 ;;
    --port)
      PORT="$2"; shift 2 ;;
    --jnlp-port)
      JNLP_PORT="$2"; shift 2 ;;
    --no-recreate)
      NO_RECREATE=1; shift ;;
    --no-restart)
      RESTART_OPT=""; shift ;;
    *)
      echo "Argumento desconocido: $1" >&2; exit 2 ;;
  esac
done

IMAGE_FULL="${IMAGE_NAME}:${TAG}"

run_cmd() {
  echo "Running: $*"
  "$@"
}

echo "==> Construyendo imagen Jenkins: $IMAGE_FULL"
run_cmd docker build -f Dockerfile.jenkins -t "$IMAGE_FULL" .

if docker ps -a --filter "name=^jenkins$" --format '{{.Names}}' | grep -q jenkins; then
  if [[ "$NO_RECREATE" == "1" ]]; then
    echo "Contenedor 'jenkins' ya existe y --no-recreate definido. Terminando."
    exit 0
  fi
  echo "==> Eliminando contenedor antiguo 'jenkins' (config persistida en jenkins_home)"
  run_cmd docker rm -f jenkins
fi

echo "==> Levantando Jenkins en puerto $PORT (jnlp $JNLP_PORT)"
# shellcheck disable=SC2086
run_cmd docker run -d $RESTART_OPT \
  --name jenkins \
  --network "$NETWORK" \
  -p "$PORT:8080" \
  -p "$JNLP_PORT:50000" \
  -v "jenkins_home:/var/jenkins_home" \
  -v "/var/run/docker.sock:/var/run/docker.sock" \
  "$IMAGE_FULL"

echo "==> Verificando acceso al daemon Docker desde el contenedor..."
run_cmd docker exec jenkins docker info --format '{{.ServerVersion}}'

echo ""
echo "Jenkins listo. Abre http://localhost:$PORT"