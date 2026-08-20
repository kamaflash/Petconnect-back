#!/usr/bin/env bash
# Entrypoint personalizado para el contenedor de Jenkins (con cliente Docker).
# Ajusta los permisos de las claves SSH montadas en /root/.ssh (necesario porque
# al montar desde Windows llegan como 0777 y OpenSSH las rechaza) y luego
# delega en el arranque estándar de la imagen jenkins/jenkins.
set -e

if [ -f /root/.ssh/id_ed25519 ]; then
    chmod 700 /root/.ssh
    chmod 600 /root/.ssh/id_ed25519 2>/dev/null || true
    chmod 644 /root/.ssh/known_hosts 2>/dev/null || true
fi

# Delegar en el arranque original de la imagen (tini -> jenkins.sh)
exec /usr/bin/tini -- /usr/local/bin/jenkins.sh "$@"